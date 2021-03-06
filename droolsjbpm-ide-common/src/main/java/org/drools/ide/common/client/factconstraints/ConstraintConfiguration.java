/*
 * Copyright 2010 JBoss Inc
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

package org.drools.ide.common.client.factconstraints;

import java.io.Serializable;
import java.util.Set;

public interface ConstraintConfiguration extends Serializable {
    public String getFactType();
    public void setFactType(String factType);
    
    public String getFieldName();
    public void setFieldName(String fieldName);
    
    public Set<String> getArgumentKeys();
    public Object getArgumentValue(String key);
    public void setArgumentValue(String key, String value);
    public boolean containsArgument(String key);

    public String getConstraintName();
    public void setConstraintName(String name);
}
