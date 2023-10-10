// Copyright 2022 Voyance Systems

/** */
package simulator.util;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import java.io.File;

/** Utility class for handling XML files */
public class XmlUtil {

  private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

  /**
   * Checks if the input xml document is valid
   *
   * @param inputXml the input xml file to be validated
   * @param schema the xml schema
   * @return true if the XML input validates to the input schema
   */
  public static boolean isDocumentValid(File inputXml, File schema) {
    boolean isValid = false;

    try {
      XMLValidationSchemaFactory schemaFactory =
          XMLValidationSchemaFactory.newInstance(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA);
      XMLValidationSchema xsd = schemaFactory.createSchema(schema);
      XMLInputFactory2 inputFactory = (XMLInputFactory2) XMLInputFactory.newInstance();
      XMLStreamReader2 streamReader = inputFactory.createXMLStreamReader(inputXml);

      streamReader.validateAgainst(xsd);

      while (streamReader.hasNext()) {
        streamReader.next();
      }

      isValid = true;
    } catch (Exception e) {
      logger.warn(
          "Error while validating {} against the schema {}",
          inputXml.getName(),
          schema.getName(),
          e);
    }

    return isValid;
  }
}
