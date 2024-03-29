/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.mojo;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData.StringType;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generate Java classes from Avro schema files (.avsc)
 *
 * @goal schema
 * @phase generate-sources
 * @threadSafe
 */
public class SchemaMojo extends AbstractAvroMojo {
    /**
     * A parser used to parse all schema files. Using a common parser will
     * facilitate the import of external schemas.
     */
    private final Schema.Parser schemaParser = new Schema.Parser();

    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern <code>**&#47;*.avsc</code>
     * is used to select grammar files.
     *
     * @parameter
     */
    private final String[] includes = new String[] { "**/*.avsc" };

    /**
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern <code>**&#47;*.avsc</code>
     * is used to select grammar files.
     *
     * @parameter
     */
    private final String[] testIncludes = new String[] { "**/*.avsc" };

    @Override
    protected void doCompile(String filename, File sourceDirectory, File outputDirectory) throws IOException {
        final File src = new File(sourceDirectory, filename);
        final Parser localSchemaParser = new Schema.Parser();
        final Map<String, Schema> types = schemaParser.getTypes();
        localSchemaParser.addTypes(types);
        final Schema schema = localSchemaParser.parse(src);

        final SpecificCompiler compiler = new SpecificCompiler(schema);
        compiler.setTemplateDir(templateDirectory);
        compiler.setStringType(StringType.valueOf(stringType));
        compiler.setFieldVisibility(getFieldVisibility());
        compiler.setCreateSetters(createSetters);
        compiler.setEnableDecimalLogicalType(enableDecimalLogicalType);
        compiler.setOutputCharacterEncoding(project.getProperties().getProperty("project.build.sourceEncoding"));
        compiler.compileToDestination(src, outputDirectory);

        final Map<String, Schema> newTypes = localSchemaParser.getTypes()
                .values()
                .stream()
                .filter(candidate -> !types.containsValue(candidate))
                .collect(Collectors.toMap(Schema::getFullName, Function.identity()));

        this.schemaParser.addTypes(newTypes);
    }

    @Override
    protected String[] getIncludes() {
        return includes;
    }

    @Override
    protected String[] getTestIncludes() {
        return testIncludes;
    }
}
