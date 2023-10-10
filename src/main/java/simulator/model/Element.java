// Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Element is a generic class, intended as superclass of all elements with id */
public abstract class Element {

  private static final Logger logger = LoggerFactory.getLogger(Element.class);
  /** Identifier of the element object */
  protected String id;

  public Element(String id) {
    this.id = id;
  }

  /**
   * Returns the identifier of the element.
   *
   * @return id of the element
   */
  public String getId() {
    return id;
  }
}
