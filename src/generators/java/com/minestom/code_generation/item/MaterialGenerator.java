package com.minestom.code_generation.item;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.minestom.code_generation.MinestomCodeGenerator;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.Objects;

public final class MaterialGenerator extends MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialGenerator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final File DEFAULT_INPUT_FILE = new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", "items.json");
    private final File itemsFile;
    private final File outputFolder;

    public MaterialGenerator() {
        this(null, null);
    }

    public MaterialGenerator(@Nullable File itemsFile) {
        this(itemsFile, null);
    }

    public MaterialGenerator(@Nullable File itemsFile, @Nullable File outputFolder) {
        this.itemsFile = Objects.requireNonNullElse(itemsFile, DEFAULT_INPUT_FILE);
        this.outputFolder = Objects.requireNonNullElse(outputFolder, DEFAULT_OUTPUT_FOLDER);
    }

    @Override
    public void generate() {
        if (!itemsFile.exists()) {
            LOGGER.error("Failed to find items.json.");
            LOGGER.error("Stopped code generation for items.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }
        // Important classes we use alot
        ClassName namespaceIDClassName = ClassName.get("net.minestom.server.utils", "NamespaceID");
        ClassName keyIDClassName = ClassName.get("net.kyori.adventure.key", "Key");

        JsonArray items;
        try {
            items = GSON.fromJson(new JsonReader(new FileReader(itemsFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find items.json.");
            LOGGER.error("Stopped code generation for items.");
            return;
        }
        ClassName itemClassName = ClassName.get("net.minestom.server.item", "Material");

        // Item
        TypeSpec.Builder itemClass = TypeSpec.classBuilder(itemClassName)
                .addSuperinterface(ClassName.get("net.kyori.adventure.key", "Keyed"))
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED");
        itemClass.addField(
                FieldSpec.builder(namespaceIDClassName, "id")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).addAnnotation(NotNull.class).build()
        );
        itemClass.addField(
                FieldSpec.builder(keyIDClassName, "key")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).addAnnotation(NotNull.class).build()
        );
        itemClass.addField(
                FieldSpec.builder(TypeName.BYTE, "maxDefaultStackSize")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        );
        itemClass.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(namespaceIDClassName, "id").addAnnotation(NotNull.class).build())
                        .addParameter(TypeName.BYTE, "maxDefaultStackSize")
                        .addStatement("this.id = id")
                        .addStatement("this.key = id.key()")
                        .addStatement("this.maxDefaultStackSize = maxDefaultStackSize")
                        .addModifiers(Modifier.PROTECTED)
                        .build()
        );
        // Override key method (adventure)
        itemClass.addMethod(
                MethodSpec.methodBuilder("key")
                        .returns(keyIDClassName)
                        .addAnnotation(Override.class)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.key")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getId method
        itemClass.addMethod(
                MethodSpec.methodBuilder("getId")
                        .returns(namespaceIDClassName)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // Use data
        for (JsonElement i : items) {
            JsonObject item = i.getAsJsonObject();

            String itemName = item.get("name").getAsString();

            itemClass.addField(
                    FieldSpec.builder(
                            itemClassName,
                            itemName
                    ).initializer(
                            "new $T($T.from($S), (byte) $L)",
                            itemClassName,
                            namespaceIDClassName,
                            item.get("id").getAsString(),
                            item.get("maxStackSize").getAsInt()
                    ).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).build()
            );
        }

        // Write files to outputFolder
        writeFiles(
                Collections.singletonList(
                        JavaFile.builder("net.minestom.server.item", itemClass.build()).build()
                ),
                outputFolder
        );
    }
}
