package io.swagger.codegen.languages;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.swagger.codegen.*;
import io.swagger.models.*;
import io.swagger.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

public class ErlangServerCodegen extends DefaultCodegen implements CodegenConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErlangServerCodegen.class);

    protected String apiVersion = "1.0.0";
    protected String apiPath = "";
    protected String packageName = "swagger";

    public ErlangServerCodegen() {
        super();

        // set the output folder here
        outputFolder = "generated-code/erlang-server";

        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
        } else {
            additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        };

        /**
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.clear();

        /**
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        // apiTemplateFiles.put(
        //         "handler.mustache",   // the template to use
        //         ".erl");       // the extension for each file to write

        /**
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "erlang-server";

        /**
         * Reserved words.  Override this with reserved words specific to your language
         */
        setReservedWordsLowerCase(
            Arrays.asList(
                "after","and","andalso","band","begin","bnot","bor","bsl","bsr","bxor","case",
                "catch","cond","div","end","fun","if","let","not","of","or","orelse","receive",
                "rem","try","when","xor"
            )
       );

        instantiationTypes.clear();
        typeMapping.clear();

        cliOptions.clear();
        cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Erlang package name (convention: lowercase).")
            .defaultValue(this.packageName));
        /**
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put("apiPath", apiPath);
        /**
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("swagger.mustache","", "swagger.yaml"));
        supportingFiles.add(new SupportingFile("app.src.mustache", "", "src" + File.separator + this.packageName + ".app.src"));
        supportingFiles.add(new SupportingFile("include/router_hrl.mustache", "", toIncludeFilePath("router", "hrl")));
        supportingFiles.add(new SupportingFile("router.mustache", "",  toSourceFilePath("router", "erl")));
        supportingFiles.add(new SupportingFile("server.mustache", "",  toSourceFilePath("server", "erl")));
        supportingFiles.add(new SupportingFile("handler.mustache", "",  toSourceFilePath("handler", "erl")));

        /**
         * Do not reload your main customizing file
        */
        if(!new java.io.File(toSourceFilePath("api_config", "erl")).exists()){
            supportingFiles.add(new SupportingFile("api_config.mustache", "",  toSourceFilePath("api_config", "erl")));
        }
        writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
    }

    @Override
    public String apiPackage() {
        return apiPath;
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "erlang-server";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates an Erlang server library using the swagger-tools project.  By default, " +
                "it will also generate service classes--which you can disable with the `-Dnoservice` environment variable.";
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultController";
        }
        return initialCaps(name);
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
     * those terms here.  This logic is only called if a variable matches the reseved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        return "_" + name;  // add an underscore to the name
    }

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String toModelName(String name) {
        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(toModelFilename(name));
    }

    @Override
    public String toOperationId(String operationId) {
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + camelize(sanitizeName("call_" + operationId)));
            operationId = "call_" + operationId;
        }

        return camelize(operationId);
    }

    @Override
    public String toModelFilename(String name) {
        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        name = sanitizeName(name);

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + camelize("model_" + name));
            name = "model_" + name; // e.g. return => ModelReturn (after camelize)
        }

        return underscore(name);
    }

    @Override
    public String toApiFilename(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        return this.packageName + "_" + name + "_handler";
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        Swagger swagger = (Swagger)objs.get("swagger");
        if(swagger != null) {
            try {
                objs.put("swagger-yaml", Yaml.mapper().writeValueAsString(swagger));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return super.postProcessSupportingFileData(objs);
    }

    protected String toSourceFilePath(String name, String extension) {
        return "src" + File.separator +  this.packageName + "_" + name + "." + extension;
    }

    protected String toIncludeFilePath(String name, String extension) {
        return "include" + File.separator + this.packageName + "_" + name + "." + extension;
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
        for (CodegenOperation op : operationList) {
            if (op.path != null) {
                op.path = op.path.replaceAll("\\{(.*?)\\}", ":$1");
            }
        }
        return objs;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
