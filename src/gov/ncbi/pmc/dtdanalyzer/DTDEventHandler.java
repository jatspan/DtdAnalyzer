/*
 * DTDEventHandler.java
 *
 * Created on November 9, 2005, 2:03 PM
 */

package gov.ncbi.pmc.dtdanalyzer;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import java.util.*;
import java.util.regex.*;

/**
 * Collects and stores information about elements, attributes, and entities
 * declared in a DTD. Class can then be queried for this information.
 * Class should only be used with a parser that supports the LexicalHandler and
 * DeclHandler interfaces. This has been tested using Xerces, which is the preferred
 * XML reader implementation.
 *
 * @author Demian Hess
 * @version 1.0 2005-11-09
 */
public class DTDEventHandler 
    implements org.xml.sax.ContentHandler, 
               org.xml.sax.ErrorHandler, 
               org.xml.sax.ext.DeclHandler, 
               org.xml.sax.ext.LexicalHandler 
{
    private Locator locator = null;                       // Receives location information from XML reader
    private DtdModule dtdModule = null;                   // Information about the top-level
                                                          // external subset
    private Attributes allAttributes = new Attributes();  // Contains all declared attributes
    private Elements allElements = new Elements();        // Contains all declared elements
    private int numOfElements = 0;                        // Element counter
    private Entities allEntities = new Entities();        // Contains all declared entities
    private SComments allSComments = new SComments();     // Contains all structured comments.
       
    /**
     * Gets the DtdModule associated with this DTD.
     */
    public DtdModule getDtdModule() {
        return dtdModule;
    }
      
    /**
     * Returns all declared Attributes 
     *
     * @return  Collection containing all declared attributes
     */    
    public Attributes getAllAttributes(){
        return allAttributes;
    }
    
    /**
     * Returns all declared Elements
     *
     * @return  Collection containing all declared elements
     */    
    public Elements getAllElements(){
        return allElements;
    }
    
    /**
     * Returns all declared Entities 
     *
     * @return  Collection containing all declared entities
     */    
    public Entities getAllEntities(){
        return allEntities;
    }
    
    /**
     * Returns the collection of structured comments
     */
    public SComments getAllSComments() {
        return allSComments;
    }
    
    /**
     * Returns location of the last declaration. This is a convenience method for
     * internal use.
     *
     * @return  Location of last declaration
     */  
    private Location getLocation() throws SAXException{
        if ( locator == null ){
            throw new SAXException("No locator provided by the parser. " +
                "The DTD cannot be processed without location information.");
        }
        
        return new Location(locator.getSystemId(), locator.getPublicId(), locator.getLineNumber());
    }
    
    // *********************** ContentHandler methods ***********************
        
    /**
     * Sets locator that identifies the DTD file in which a declaration occurs.
     * The locator must be set in order to process a DTD. If the locator remains
     * null, the class methods will generate a SAXException.
     *
     * @param locator Reports location information from the DTD during parsing
     */    
    public void setDocumentLocator(org.xml.sax.Locator locator) {
        this.locator = locator;
    }

    /**
     * Reinitializes all values so that declaration information can be collected
     *
     * @throws SAXException Indicates problem occurred during processing 
     */    
    public void startDocument() throws org.xml.sax.SAXException {
        //System.out.println("startDocument\n");
        allAttributes = new Attributes();
        allElements = new Elements();  
        allEntities = new Entities();
        numOfElements = 0;
    }

    /**
     * Ends DTD processing because the parser has reached content. This method
     * should never be encountered if the document has a DTD because processing
     * will end when endDTD is called. Some documents, however, may not have a 
     * DTD.
     *
     * @param str         Uri of namespace
     * @param str1        localName of element
     * @param str2        Full name of element, including prefix
     * @param attributes  Element attributes
     * @throws EndOfDTDException Indicates that processing should end
     */
    public void startElement(String str, String str1, String str2, 
        org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        throw new EndOfDTDException();
    }
    
    // ++++ NOT IMPLEMENTED ++++
    public void characters(char[] values, int param, int param2) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void endDocument() throws org.xml.sax.SAXException {
        //do nothing
    }
    public void endElement(String str, String str1, String str2) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void endPrefixMapping(String str) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void ignorableWhitespace(char[] values, int param, int param2) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void processingInstruction(String str, String str1) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void skippedEntity(String str) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void startPrefixMapping(String str, String str1) throws org.xml.sax.SAXException {
        //do nothing
    }
        
    // *********************** DeclHandler methods *********************** 
    
    /**
     * Creates a new Attribute using the declaration information provided
     * by the XML Reader
     *
     * @param eName          Element name
     * @param aName          Attribute name
     * @param type           Type of content of the attribute (eg, CDATA)
     * @param valueDefault   The mode, such as "#IMPLIED"; null if not specified
     * @param value          Default value or null if none
     * @throws SAXException  Thrown if the XML Reader did not supply a Locator or some other problem occurred
     */    
    public void attributeDecl(String eName, String aName, String type, 
        String valueDefault, String value) throws org.xml.sax.SAXException {
        
        // Build the attribute: note that some values may be null and shouldn't
        // be set. Also note that building the location may throw an exception
        // if no locator was provided.
        Attribute att = new Attribute( eName, aName, type );
        
        if ( valueDefault != null ){
           att.setMode( valueDefault );
        }//if
        
        if ( value != null ){
           att.setDefaultValue( value );   
        }//if
       
        att.setLocation( getLocation() );
        
        // Add this to the master set of attributes
        allAttributes.addAttribute(att);
    }
    
    /**
     * Builds Element from the declaration information provided by the parser
     *
     * @param name          Element name 
     * @param model         Content model
     * @throws SAXException Thrown if the XML Reader did not supply a Locator or some other problem occurred
     */    
    public void elementDecl(String name, String model) throws org.xml.sax.SAXException {
        // Count how many elements there are so that we know the order in which they were
        // declared
        numOfElements++;

        // Create the element        
        Element el = new Element( name, model, numOfElements);
        el.setLocation( getLocation() );
        allElements.addElement(el);      
    }
    
    /**
     * Builds Entity from the declaration information provided by the parser
     *
     * @param name           Enity name
     * @param publicId       Public id (if supplied)
     * @param systemId       System id 
     * @throws SAXException  Thrown if an Entity object cannot be instantiated (such as due to an invalid "type")  
     */    
    public void externalEntityDecl(String name, String publicId, String systemId) throws org.xml.sax.SAXException {
        int type;
        Entity entity;
        
        try{
            if ( name.trim().startsWith("%") ){
                entity = new Entity(name.trim().substring(1), Entity.PARAMETER_ENTITY);
            }
            else{
                entity = new Entity(name.trim(), Entity.GENERAL_ENTITY);
            }
        }
        catch (Exception e){
           throw new SAXException( e.getMessage() );    
        }
        
        entity.setSystemId(systemId);       
        entity.setLocation(getLocation());
        
        if ( publicId != null ){
            entity.setPublicId(publicId);
        }
        
        allEntities.addEntity(entity);
    }
    
    /**
     * Builds Entity from declaration information provided by the parser
     *
     * @param name           Entity name
     * @param value          Entity value
     * @throws SAXException  Thrown if Entity object cannot be instantiated (such as due to an invalid "type") 
     */    
    public void internalEntityDecl(String name, String value) throws org.xml.sax.SAXException {
        int type;
        Entity entity;
        
        try{
            if ( name.trim().startsWith("%") ){
                entity = new Entity(name.trim().substring(1), Entity.PARAMETER_ENTITY);
            }
            else{
                entity = new Entity(name.trim(), Entity.GENERAL_ENTITY);
            }
        }
        catch (Exception e){
            throw new SAXException( e.getMessage() );
        }
        
        entity.setValue(value);       
        entity.setLocation(getLocation());
               
        allEntities.addEntity(entity);
    }

    // *********************** ErrorHandler methods ***********************
    
    /**
     * Stops processing of the DTD because a fatal error occured. 
     *
     * @param sAXParseException The fatal error that stopped processing
     * @throws SAXException  This will probably never be thrown */    
    public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
        throw sAXParseException;
    }
    
    // ++++ NOT IMPLEMENTED ++++ 
    public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
        //do nothing
    }
    
    // *********************** LexicalHandler methods ***********************
    
    /**
     * Signals that the DTD has been fully processed
     *
     * @throws SAXException Subclass indicates that the DTD has been processed  */    
    public void endDTD() throws org.xml.sax.SAXException {
        throw new EndOfDTDException();
    }
    
    /**
     * Handle comments.  This extracts the special annotations and stores them
     * for later.
     */
    public void comment(char[] text, int start, int length) 
        throws org.xml.sax.SAXException 
    {
        try {
            String fullComment = new String(text, start, length).trim();
            
            // Make sure this is a special comment
            if (!fullComment.startsWith("~~")) return;
    
            // Match the beginning and ending "~~", and the identifier line.       
            Pattern p = Pattern.compile("\\A~~[ \\t]*(.*?)[ \\t]*(\\n.*)~~", Pattern.DOTALL);
            Matcher m = p.matcher(fullComment);
            if (!m.find()) {
                throw new Exception("Malformed structured comment");
            }
            
            // Pull out the identifier, and create a new SComment object
            String identifier = m.group(1);
            SComment sc = new SComment(identifier);
            
            // If this comment type is MODULE, then the name has to come from the system id
            // of the beast that we are currently parsing.  We'll convert this into a 
            // relative URI, and use that for the name.
            if (sc.getType() == SComment.MODULE) {
                String curSysId = locator.getSystemId();
                String relSysId = dtdModule.relativize(curSysId);
                sc.setName(relSysId);
            }
    
            // comment will hold everything from the newline at the end of identifier line, 
            // and up to but not including the final ~~.
            String comment = m.group(2);
            
            // Next we'll parse the rest of the comment
            
            // This pattern matches the current section, if there is one, plus the intro 
            // of the next section, if there is one.
            p = Pattern.compile("(.*?)((\\n[ \\t]*~~[ \\t]*(\\S+))|\\z)", Pattern.DOTALL);
            
            // sectionName stores the name of the next annotation section.  The first section is 
            // implicitly defined to be "notes".
            String sectionName = "notes";
            
            // sp points to the next character in the input string that we want to match
            int sp = 0;  
            // We'll find each annotation section until done
            boolean done = false;
            while (!done) {
                m = p.matcher(comment.substring(sp));
                if (m.find()) {
                    String sectionText = m.group(1);
                    sp += m.end();
                    
                    // Unless we're looking at an empty "notes", add this section 
                    // to the SComment
                    if (!sectionName.equals("notes") || !sectionText.trim().equals("")) {
                        sc.addSection(sectionName, sectionText);
                    }
                    
                    // If there's no next intro line, then we're done.
                    if (m.group(2).equals("")) {
                        done = true;
                    }
                    else {
                        sectionName = m.group(4);
                    }
                }
                else {
                    // I don't think we'll ever see this.
                    throw new Exception("Malformed annotated comment section, starting at " + 
                                        locator.getSystemId() + ", line " +
                                        locator.getLineNumber());
                }
            }
            
            // Add this SComment object to the collection
            allSComments.addSComment(sc);
        }
        catch (Exception e) {
            System.err.println("Error while interpreting structured comment ending at\n" + 
                locator.getSystemId() + ", line " + locator.getLineNumber() + ".\n" +
                "Did you remember to use the right comment processor?\n" +
                e.getMessage() );
            System.exit(1);
        }
    }


    public void endCDATA() throws org.xml.sax.SAXException {
        //do nothing
    }
    public void endEntity(String str) throws org.xml.sax.SAXException {
        //do nothing
    }
    public void startCDATA() throws org.xml.sax.SAXException {
        //do nothing
    }
    public void startDTD(String str, String str1, String str2) throws org.xml.sax.SAXException {
        //do nothing
    }
    
    /**
     * Handler for the start of entities.  We use this to find information about "modules", which
     * are files that make up the DTD.
     */
    public void startEntity(String str) throws org.xml.sax.SAXException {
        try {
            if (str.equals("[dtd]")) {
                dtdModule = new DtdModule(locator);
                Entity.setBaseUri(dtdModule.getBaseUri());
            }
    
            else if (str.charAt(0) == '%') {
                Entity e = allEntities.getEntity(str.substring(1), Entity.PARAMETER_ENTITY);
                if (e.isExternal()) {
                    e.setIncluded(true);
                }
            }
        }
        catch (Exception e) {
            // this should never happen.
        }
    }
    
} //DTDEventHandler
