/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service;

import annis.model.Annotation;

/**
 * Interface defining the REST API calls that ANNIS provides for getting the
 * meta data.
 *
 * <p>
 * All paths for this part of the service start with
 * <pre>annis/meta/</pre>
 * </p>
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface MetadataService
{

  /**
   * Fetches the meta data for a top level corpus.
   *
   * <p>If the <b>deep</b> path is called, the metadata of all subcorpora and
   * documents are also fetched.</p>
   *
   *
   * <h3>Path(s)</h3>
   *
   * <ol>
   * <li>GET annis/meta/<b>{top}</b></li>
   * <li>GET annis/meta/<b>{top}</b>/<b>{doc}</b></li>
   * </ol>
   *
   * <h3>MIME</h3>
   *
   * accept:
   * <code>application/xml</code>
   *
   * @param topLevelCorpus Determines the corpus, for which the annotations are
   * retrieved.
   * @return The xml representation of a list wich contains {@link Annotation}
   * objects.
   */
  public Annotation getMetadata(String topLevelCorpus);

  /**
   * Fetches the meta data for a top level corpus.
   *
   * <h3>Path(s)</h3>
   *
   * <ol>
   * <li>GET annis/meta/deep/<b>{top}</b></li>
   * <li>GET annis/meta/deep/<b>{top}</b>/<b>{doc}</b></li>
   *
   * </ol>
   *
   * <h3>MIME</h3>
   *
   * accept:
   * <code>application/xml</code>
   *
   * @param topLevelCorpus Determines the corpus, for which the annotations are
   * retrieved.
   * @param docname Determines the document name, for which the annotations are
   * fetched. If null, all annotations of all documents are fetched.
   * @return The xml representation of a list wich contains {@link Annotation}
   * objects.
   */
  public Annotation getMetadataDeep(String topLevelCorpus, String docname);

  /**
   * Fetches all document names within a top level corpus.
   *
   * <h3>Path</h3>
   *
   * <p>GET annis/meta/docnames/<b>{top}</b></p> *
   *
   * <h3>MIME</h3>
   *
   * accept:
   * <code>application/xml</code>
   *
   * @param topLevelCorpus Determines the corpus, for which the document names
   * are retrieved.
   * @return The xml representation of a list wich contains {@link Annotation}
   * objects.
   */
  public Annotation getDocNames(String topLevelCorpus);
}
