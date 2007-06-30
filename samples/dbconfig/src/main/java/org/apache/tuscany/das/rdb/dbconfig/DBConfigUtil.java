/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.tuscany.das.rdb.dbconfig;


import java.io.IOException;
import java.io.InputStream;

import org.apache.tuscany.sdo.util.SDOUtil;

import commonj.sdo.helper.XMLHelper;

/**
 * Config util provides config-related utilities such as loading a Config
 * instance from an InputStream
 * 
 */
public final class DBConfigUtil {

    private DBConfigUtil() {
    }
    
    public static DBConfig loadDBConfig(InputStream dbconfigStream) {

        if (dbconfigStream == null) {
            throw new RuntimeException("Cannot load configuration from a null InputStream. "
                    + "Possibly caused by an incorrect config xml file name");
        }

        SDOUtil.registerStaticTypes(DbconfigFactory.class);
        XMLHelper helper = XMLHelper.INSTANCE;

        try {
            return (DBConfig) helper.load(dbconfigStream).getRootObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}