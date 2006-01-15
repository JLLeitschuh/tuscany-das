/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.das.rdb.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

import org.apache.tuscany.das.rdb.ApplyChangesCommand;
import org.apache.tuscany.das.rdb.Command;
import org.apache.tuscany.das.rdb.Key;
import org.apache.tuscany.das.rdb.config.Config;
import org.apache.tuscany.das.rdb.config.ConfigPackage;
import org.apache.tuscany.das.rdb.config.ConnectionProperties;
import org.apache.tuscany.das.rdb.util.DebugUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;

import commonj.sdo.DataObject;
import commonj.sdo.Type;

/**
 * 
 */
public class ApplyChangesCommandImpl implements ApplyChangesCommand {

    private ConnectionImpl dasConnection;

    private static final boolean debug = false;

    private ChangeSummarizer summarizer = new ChangeSummarizer();

    public ApplyChangesCommandImpl() {
        // Empty Constructor
    }

    // IOException is recoverable, needs to be thrown
    public ApplyChangesCommandImpl(InputStream mappingModel) throws IOException {
        setMapping(mappingModel);
    }

    public ApplyChangesCommandImpl(Config mappingModel){
        summarizer.setMapping(mappingModel);
        if (mappingModel.getConnectionProperties() != null)
            setConnection(mappingModel.getConnectionProperties());
    }

    public void setConnection(Connection connection) {
        setConnection(new ConnectionImpl(connection));
    }

    public void setConnection(Connection connection, boolean manageTransaction) {
        ConnectionImpl c = new ConnectionImpl(connection);
        c.setManageTransactions(manageTransaction);
        setConnection(c);
    }

    public void setConnection(ConnectionImpl connection) {
        dasConnection = connection;
        summarizer.setConnection(connection);
    }

    public void setConnection(ConnectionProperties c) {
        try {
            Connection connection = null;
            Class.forName(c.getDriverClassName());
            if (c.getDriverUserName() == null)
                connection = DriverManager.getConnection(c.getDriverURL());
            else
                connection = DriverManager.getConnection(c.getDriverURL(), c.getDriverUserName(), c.getDriverPassword());
            connection.setAutoCommit(false);
            setConnection(connection);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addCreateCommand(Type type, Command cmd) {
        summarizer.addCreateCommand(type, cmd);
        ((CommandImpl) cmd).setConnection(dasConnection);
    }

    public void addUpdateCommand(Type type, Command cmd) {
        // DebugUtil.debugln(getClass(), debug, "Adding update command for type
        // " + type.getName());
        summarizer.addUpdateCommand(type, cmd);
        ((CommandImpl) cmd).setConnection(dasConnection);
    }

    public void addDeleteCommand(Type type, Command cmd) {
        summarizer.addDeleteCommand(type, cmd);
        ((CommandImpl) cmd).setConnection(dasConnection);
    }

    public void execute(DataObject root) {
        DebugUtil.debugln(getClass(), debug, "Executing ApplyChangesCmd");

        if (dasConnection == null)
            throw new RuntimeException("A connection must be provided");

        Changes changes = summarizer.loadChanges(root);

        boolean success = false;
        try {
            changes.execute();
            success = true;
        } finally {
            if (success)
                dasConnection.cleanUp();
            else
                dasConnection.errorCleanUp();
        }
    }

    public void setMapping(InputStream stream) throws IOException {
        XMLResource resource = new XMLResourceImpl();

        HashMap map = new HashMap();
        ExtendedMetaData metadata = ExtendedMetaData.INSTANCE;
        metadata.putPackage(null, ConfigPackage.eINSTANCE);

        map.put(XMLResource.NO_NAMESPACE_SCHEMA_LOCATION, ConfigPackage.eNS_URI);
        map.put(XMLResource.OPTION_EXTENDED_META_DATA, metadata);

        resource.load(stream, map);
        Config mapping = (Config) resource.getContents().get(0);
        summarizer.setMapping(mapping);
        if (mapping.getConnectionProperties() != null)
            setConnection(mapping.getConnectionProperties());

    }

    public void addRelationship(String parent, String child) {
        summarizer.addRelationship(parent, child);
    }

    public void addPrimaryKey(String columnName) {
        summarizer.addPrimaryKey(columnName);
    }

    public void addCollisionColumn(String columnName) {
        summarizer.addCollisionColumn(columnName);
    }

    public void addRelationship(Key parentKey, Key childKey) {
        throw new RuntimeException("Not Implemented");
    }

    public void addPrimaryKey(Key key) {
        summarizer.addPrimarykey(key);
    }

    public void addGeneratedPrimaryKey(String columnName) {
        summarizer.addGeneratedPrimaryKey(columnName);
    }

    public void addConverter(String name, String converterName) {
        summarizer.addConverter(name, converterName);
    }

}