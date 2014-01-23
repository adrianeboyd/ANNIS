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
package annis.libgui;

import java.util.Set;

/**
 *
 * Wraps the information, which token annotation should be shown in
 * Visualizations. E.g. this is used in the KWICPanel.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface VisibleTokenAnnoChanger
{

  /**
   * The qualified names (foo::bar) of the annotations, which should not be
   * displayed.
   *
   * @param tokenAnnotationLevelSet Set of annotations. May not be null.
   */
  public void updateVisibleToken(Set<String> tokenAnnotationLevelSet);
}
