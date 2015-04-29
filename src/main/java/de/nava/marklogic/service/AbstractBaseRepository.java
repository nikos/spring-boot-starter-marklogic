package de.nava.marklogic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.document.TextDocumentManager;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.extra.jdom.JDOMHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.TuplesHandle;
import com.marklogic.client.query.*;
import de.nava.marklogic.domain.view.*;
import de.nava.marklogic.utils.StringBucketComparator;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Base operations common to all services accessing the document repository
 * via the MarkLogic client API. Methods cover search (incl. auto-suggestion,
 * basic update operations and co-occurrence queries for data analysis needs).
 *
 * @author Niko Schmuck
 */
@Service
public abstract class AbstractBaseRepository<T> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${search.pagelength}")
    protected int defaultPageLength;

    @Autowired
    protected MarkLogicConnections connections;

    @Autowired
    protected ObjectMapper mapper;

    public abstract String getOptionsName();
    public abstract String getDocumentDirectory();

    /**
     * Returns an array of suggested strings matching to the given term.
     */
    public String[] getSuggestedTerms(String username, String q) {
        QueryManager queryManager = connections.getQueryManager(username);
        SuggestDefinition suggestDef = queryManager.newSuggestDefinition(q, getOptionsName());
        String[] terms = queryManager.suggest(suggestDef);
        logger.info("Suggestions for '{}' returned {} terms", q, terms.length);
        return terms;
    }

    /**
     * Triggers search by the given "Suchschlitz" string in MarkLogic server.
     *
     * @param page The page number of results to return (1 based), the
     *             page length is configurable via the property <code>search.pagelength</code>.
     */
    public SearchHandle findAll(final String username, final String query, final int page) {
        long start = (page - 1) * defaultPageLength + 1;

        QueryManager queryManager = connections.getQueryManager(username);
        StringQueryDefinition qdef = queryManager.newStringDefinition(getOptionsName());

        qdef.setCriteria(query);
        qdef.setDirectory(getDocumentDirectory());

        SearchHandle resultsHandle = new SearchHandle();
        queryManager.search(qdef, resultsHandle, start);
        logger.info("Search for '{}' returned {} results (p. {})", query, resultsHandle.getTotalResults(), page);

        return resultsHandle;
    }

    public long getTotalPages(SearchHandle resultsHandle) {
        return Math.round(Math.ceil(1.0 * resultsHandle.getTotalResults() / defaultPageLength));
    }

    public long getPageNumber(SearchHandle resultsHandle) {
        return Math.round((resultsHandle.getStart() - 1) / defaultPageLength + 1);
    }

    public int getDefaultPageLength() {
        return defaultPageLength;
    }

    // ~~

    public JsonNode getDocumentAsJSON(String username, String uri) {
        JSONDocumentManager docMgr = connections.getJSONDocumentManager(username);
        return docMgr.read(uri, new JacksonHandle()).get();
    }

    public Document getDocumentAsXML(String username, String uri) {
        XMLDocumentManager xmlDocumentManager = connections.getXMLDocumentManager(username);
        return xmlDocumentManager.read(uri, new JDOMHandle()).get();
    }

    public String getDocumentAsText(String username, String uri) {
        TextDocumentManager textDocumentManager = connections.getTextDocumentManager(username);
        return textDocumentManager.read(uri, new StringHandle()).get();
    }


    /**
     * Extracts typed objects out of the raw search result.
     */
    public Collection<T> getResultObjects(String username, SearchHandle searchResult) {
        Collection<T> objects = new ArrayList<>();
        for (MatchDocumentSummary summary : searchResult.getMatchResults()) {
            String uri = summary.getUri();
            try {
                logger.debug("  * {}: {}", uri, summary.getFormat());
                if (uri.endsWith(".json")) {
                    JsonNode doc = getDocumentAsJSON(username, uri);
                    objects.add(fromJSON(doc, summary.getFirstSnippetText()));
                } else if (uri.endsWith(".xml")) {
                    Document doc = getDocumentAsXML(username, uri);
                    objects.add(fromDOM(doc, summary.getFirstSnippetText()));
                } else {
                    logger.info("Skipped mapping, since document type unsupported for {} ", uri);
                }
            } catch (Exception e) {
                logger.warn("Unable to parse {} into object: {}", uri, e.getMessage());
            }
        }
        return objects;
    }

    /**
     * Maps an XML DOM to a domain specific object.
     */
    protected abstract T fromDOM(Document doc, String snippetText) throws Exception;

    /**
     * Maps an JSON tree representation to a domain specific object.
     */
    protected abstract T fromJSON(JsonNode doc, String snippetText) throws JsonProcessingException;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Update operations
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Writes the given JSON structure to the database under the
     * specified URI as document ID.
     */
    public void saveJSON(String username, String uri, JsonNode doc) {
        JSONDocumentManager jsonDocumentManager = connections.getJSONDocumentManager(username);
        logger.debug("Writing document '{}' ...", uri);
        // TODO: also specify collection to save document in (via abstract method getCollection()...)
        jsonDocumentManager.write(uri, new JacksonHandle(doc));
    }

    public void updateJSON(String username, String uri, String selElem, String posElem, JsonNode fragmentNode) {
        JSONDocumentManager jsonDocumentManager = connections.getJSONDocumentManager(username);
        logger.info("Updating document '{}' for '{}' ...", uri, selElem);

        // TODO: for the time being this is the most straight forward solution
        ObjectNode doc = (ObjectNode) getDocumentAsJSON(username, uri);
        doc.set(selElem, fragmentNode);
        jsonDocumentManager.write(uri, new JacksonHandle(doc));

        /*
        try {
            // TODO: until we have not fixed the replaceInsert Dilemma we have to use two operations
            // (A) delete existing property
            DocumentPatchHandle patch = jsonDocumentManager.newPatchBuilder()
                //.deleteProperty(selElem).build();
                .delete("/" + selElem).build();  // TODO: this will not work with arrays
            jsonDocumentManager.patch(uri, patch);

            // (B) insert new element
            String fragment = String.format("{\"%s\":%s}", selElem, mapper.writeValueAsString(fragmentNode));
            logger.info(" -->> " + fragment);
            patch = jsonDocumentManager.newPatchBuilder()
                .insertFragment("/"+posElem, DocumentPatchBuilder.Position.AFTER, fragment).build();
            jsonDocumentManager.patch(uri, patch);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to update document " + uri + " at " + selElem);
        }
        */
    }

    public void handleAll(String username, Operator<T> op, int startPage) {
        int pageNr = startPage;
        long maxPages;
        do {
            SearchHandle searchResult = findAll(username, "", pageNr);
            maxPages = getTotalPages(searchResult);
            logger.info("Handling individual objects (p. {} of {}) ...", pageNr, maxPages);
            Collection<T> objs = getResultObjects(username, searchResult);
            for (T o : objs) {
                logger.debug("  * operating on '{}'...", o);
                op.op(o);
            }
            pageNr++;
        } while (pageNr <= maxPages);
        logger.info("Completed handling of all objects.");
    }

    public interface Operator<T> {
        void op(T object);
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Co-occurrences
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Returns co-occurrence data structure by the given <code>tupleName</code> which represent
     * the number of co-occurrences (for example over two facets).
     */
    public Cooccurrences2D getCooccurrences2D(final String username, final String tupleName) {

        QueryManager queryManager = connections.getQueryManager(username);

        ValuesDefinition vdef = queryManager.newValuesDefinition(tupleName, getOptionsName());

        TuplesHandle tuples = queryManager.tuples(vdef, new TuplesHandle());

        Map<String, Integer> xCategories = new HashMap<>();
        Map<String, Integer> yCategories = new HashMap<>();
        List<Occurrence2D> occurrence2Ds = new ArrayList<>();

        for (Tuple tuple : tuples.getTuples()) {
            // assert that there are always two values per tuple available, assume also type xs:string
            String xCategory = tuple.getValues()[0].get(String.class);
            Integer x = xCategories.get(xCategory);
            if (x == null) {
                x = xCategories.size();
                xCategories.put(xCategory, x);
            }

            String yCategory = tuple.getValues()[1].get(Integer.class).toString();
            Integer y = yCategories.get(yCategory);
            if (y == null) {
                y = yCategories.size();
                yCategories.put(yCategory, y);
            }

            Coordinate coord = new Coordinate(x, y);
            occurrence2Ds.add(new Occurrence2D(coord, tuple.getCount()));
        }

        return new Cooccurrences2D(xCategories, yCategories, occurrence2Ds);
    }

    public CooccurrencesSeries getCooccurrencesSeries(final String username, final String tupleName) {
        return getCooccurrencesSeries(username, tupleName, null);
    }

    public CooccurrencesSeries getCooccurrencesSeries(final String username, final String tupleName, ColorMappingService colorMappingService) {
        QueryManager queryManager = connections.getQueryManager(username);

        ValuesDefinition vdef = queryManager.newValuesDefinition(tupleName, getOptionsName());

        TuplesHandle tuples = queryManager.tuples(vdef, new TuplesHandle());

        // Map<Y, SortedList{X+Cnt}>:  Y:Accept, values [ (20J:3), (25J:5), ....]
        Map<String, Set<Occurrence1D>> results = new TreeMap<>();
        Set<String> uniqueXCategories = new TreeSet<>(new StringBucketComparator());

        for (Tuple tuple : tuples.getTuples()) {
            // assert that there are always two values per tuple available, assume also type xs:string
            //String yCategory = tuple.getValues()[1].get(String.class);
            String yCategory = (tuple.getValues()[1].get(Integer.class)).toString();
            String xCategory = tuple.getValues()[0].get(String.class);
            uniqueXCategories.add(xCategory);

            Set<Occurrence1D> xList = results.get(yCategory);
            if (xList == null) {
                xList = new TreeSet<>();
            }
            xList.add(new Occurrence1D(xCategory, tuple.getCount()));
            results.put(yCategory, xList);
        }

        // Fill in missing / normalize so that all lists do contain the same elements
        for (Map.Entry<String, Set<Occurrence1D>> ySet : results.entrySet()) {
            for (String xCat : uniqueXCategories) {
                boolean containsKey = false;
                for (Occurrence1D occ : ySet.getValue()) {
                    if (occ.key.equals(xCat)) {
                        containsKey = true;
                        break;
                    }
                }
                if (!containsKey) {
                    ySet.getValue().add(new Occurrence1D(xCat, 0L));
                }
            }
        }

        return new CooccurrencesSeries(results, uniqueXCategories, colorMappingService);
    }

}

