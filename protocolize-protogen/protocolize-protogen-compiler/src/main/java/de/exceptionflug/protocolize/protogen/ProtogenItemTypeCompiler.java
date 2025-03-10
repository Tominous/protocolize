package de.exceptionflug.protocolize.protogen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.exceptionflug.protocolize.items.ItemIDMapping;
import de.exceptionflug.protocolize.items.ItemStack;
import de.exceptionflug.protocolize.items.ItemType;
import de.exceptionflug.protocolize.items.SpawnEggItemIDMapping;
import org.objectweb.asm.Label;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class ProtogenItemTypeCompiler {

    public static void compile(final File input, final File targetDir) throws Exception {
        final Map<String, Class> params = new LinkedHashMap<>();
        params.put("maxStackSize", int.class);
        params.put("mappings", ItemIDMapping[].class);

        final Constructor constructor = new Constructor(Modifier.PRIVATE, params, methodVisitor -> {
            final Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(13, l1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ILOAD, 3);
            methodVisitor.visitFieldInsn(PUTFIELD, "de/exceptionflug/protocolize/items/ItemType", "maxStackSize", "I");
            final Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(14, l2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitFieldInsn(PUTFIELD, "de/exceptionflug/protocolize/items/ItemType", "mappings", "[Lde/exceptionflug/protocolize/items/ItemIDMapping;");
            final Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(15, l3);
            methodVisitor.visitInsn(RETURN);
        });

        final EnumBuilder enumBuilder = new EnumBuilder("de/exceptionflug/protocolize/items/ItemType", "ItemType.proto")
                .addField(new Field("mappings", ItemIDMapping[].class, Modifier.PRIVATE + Modifier.FINAL))
                .addField(new Field("maxStackSize", int.class, Modifier.PRIVATE + Modifier.FINAL))
                .addConstructor(constructor)
                .addMethod(new Method("getMaxStackSize", ACC_PUBLIC, Collections.emptySortedMap(), int.class, methodVisitor -> {
                    final Label l0 = new Label();
                    methodVisitor.visitLabel(l0);
                    methodVisitor.visitLineNumber(18, l0);
                    methodVisitor.visitVarInsn(ALOAD, 0);
                    methodVisitor.visitFieldInsn(GETFIELD, "de/exceptionflug/protocolize/items/ItemType", "maxStackSize", "I");
                    methodVisitor.visitInsn(IRETURN);
                    final Label l1 = new Label();
                    methodVisitor.visitLabel(l1);
                    methodVisitor.visitLocalVariable("this", "Lde/exceptionflug/protocolize/items/ItemType;", null, l0, l1, 0);
                }))
                .addMethod(makeGetType0())
                .addMethod(makeGetType1())
                .addMethod(makeGetApplicableMapping());

        final JsonArray root = (JsonArray) new JsonParser().parse(new FileReader(input));
        for(final JsonElement element : root) {
            final JsonObject obj = element.getAsJsonObject();
            final String name = obj.get("name").getAsString();
            final int maxStackSize = obj.get("maxStackSize").getAsInt();
            final JsonArray mappings = obj.get("mappings").getAsJsonArray();
            enumBuilder.addMethod(new Method("proto"+name, Modifier.PRIVATE + Modifier.STATIC, Collections.emptySortedMap(), ItemIDMapping[].class, methodVisitor -> {
                final Label l0 = new Label();
                methodVisitor.visitLabel(l0);
                methodVisitor.visitLineNumber(10, l0);
                methodVisitor.visitIntInsn(SIPUSH, mappings.size());
                methodVisitor.visitTypeInsn(ANEWARRAY, "de/exceptionflug/protocolize/items/ItemIDMapping");
                methodVisitor.visitInsn(DUP);
                int index = 0;
                for(final JsonElement jsonElement : mappings) {
                    final JsonObject mapping = jsonElement.getAsJsonObject();
                    final Class<?> type;
                    try {
                        type = Class.forName(mapping.get("type").getAsString());
                        final int protocolStart = mapping.get("protocolRangeStart").getAsInt();
                        final int protocolEnd = mapping.get("protocolRangeEnd").getAsInt();
                        methodVisitor.visitIntInsn(BIPUSH, index);
                        methodVisitor.visitTypeInsn(NEW, type.getName().replace(".", "/"));
                        methodVisitor.visitInsn(DUP);
                        index ++;
                        if(type.equals(ItemIDMapping.class)) {
                            final int id = mapping.get("id").getAsInt();
                            if(mapping.has("durability")) {
                                final int durability = mapping.get("durability").getAsInt();
                                methodVisitor.visitIntInsn(SIPUSH, protocolStart);
                                methodVisitor.visitIntInsn(SIPUSH, protocolEnd);
                                methodVisitor.visitIntInsn(SIPUSH, id);
                                methodVisitor.visitIntInsn(SIPUSH, durability);
                                methodVisitor.visitMethodInsn(INVOKESPECIAL, "de/exceptionflug/protocolize/items/ItemIDMapping", "<init>", "(IIII)V", false);
                            } else {
                                methodVisitor.visitIntInsn(SIPUSH, protocolStart);
                                methodVisitor.visitIntInsn(SIPUSH, protocolEnd);
                                methodVisitor.visitIntInsn(SIPUSH, id);
                                methodVisitor.visitMethodInsn(INVOKESPECIAL, "de/exceptionflug/protocolize/items/ItemIDMapping", "<init>", "(III)V", false);
                            }
                        } else if(type.equals(SpawnEggItemIDMapping.class)) {
                            final String id = mapping.get("id").getAsString();
                            methodVisitor.visitIntInsn(SIPUSH, protocolStart);
                            methodVisitor.visitIntInsn(SIPUSH, protocolEnd);
                            methodVisitor.visitLdcInsn(id);
                            methodVisitor.visitMethodInsn(INVOKESPECIAL, "de/exceptionflug/protocolize/items/SpawnEggItemIDMapping", "<init>", "(IILjava/lang/String;)V", false);
                        }
                        methodVisitor.visitInsn(AASTORE);
                        if(index != mappings.size()) {
                            methodVisitor.visitInsn(DUP);
                        }
                    } catch (final ClassNotFoundException e) {
                        System.out.println("Please check classpath: "+e.getMessage());
                        continue;
                    }
                }
                methodVisitor.visitInsn(ARETURN);
            }));
            enumBuilder.addConstant(new Constant(name, methodVisitor -> {
                methodVisitor.visitIntInsn(BIPUSH, maxStackSize);
                methodVisitor.visitMethodInsn(INVOKESTATIC, "de/exceptionflug/protocolize/items/ItemType", "proto"+name, "()[Lde/exceptionflug/protocolize/items/ItemIDMapping;", false);
            }, constructor));
        }

        final File target = new File(targetDir,"ItemType.class");
        target.createNewFile();
        try(final FileOutputStream out = new FileOutputStream(target)) {
            out.write(enumBuilder.buildByteArray());
            out.flush();
        }
    }

    private static Method makeGetApplicableMapping() {
        final Map<String, Class> params = new LinkedHashMap<>();
        params.put("protocolVersion", int.class);
        return new Method("getApplicableMapping", ACC_PUBLIC, params, ItemIDMapping.class, methodVisitor -> {
            final Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(836, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "de/exceptionflug/protocolize/items/ItemType", "mappings", "[Lde/exceptionflug/protocolize/items/ItemIDMapping;");
            methodVisitor.visitVarInsn(ASTORE, 2);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitInsn(ARRAYLENGTH);
            methodVisitor.visitVarInsn(ISTORE, 3);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 4);
            final Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitFrame(F_APPEND,3, new Object[] {"[Lde/exceptionflug/protocolize/items/ItemIDMapping;", INTEGER, INTEGER}, 0, null);
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 3);
            final Label l2 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPGE, l2);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitInsn(AALOAD);
            methodVisitor.visitVarInsn(ASTORE, 5);
            final Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(837, l3);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "de/exceptionflug/protocolize/items/ItemIDMapping", "getProtocolVersionRangeStart", "()I", false);
            methodVisitor.visitVarInsn(ILOAD, 1);
            final Label l4 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPGT, l4);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "de/exceptionflug/protocolize/items/ItemIDMapping", "getProtocolVersionRangeEnd", "()I", false);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitJumpInsn(IF_ICMPLT, l4);
            final Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLineNumber(838, l5);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(836, l4);
            methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
            methodVisitor.visitIincInsn(4, 1);
            methodVisitor.visitJumpInsn(GOTO, l1);
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(840, l2);
            methodVisitor.visitFrame(F_CHOP,3, null, 0, null);
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitInsn(ARETURN);
            final Label l6 = new Label();
            methodVisitor.visitLabel(l6);
            methodVisitor.visitLocalVariable("mapping", "Lde/exceptionflug/protocolize/items/ItemIDMapping;", null, l3, l4, 5);
            methodVisitor.visitLocalVariable("this", "Lde/exceptionflug/protocolize/items/ItemType;", null, l0, l6, 0);
            methodVisitor.visitLocalVariable("protocolVersion", "I", null, l0, l6, 1);
        });
    }

    private static Method makeGetType1() {
        final Map<String, Class> params = new LinkedHashMap<>();
        params.put("id", int.class);
        params.put("durability", short.class);
        params.put("protocolVersion", int.class);
        params.put("stack", ItemStack.class);
        return new Method("getType", ACC_PUBLIC + ACC_STATIC, params, ItemType.class, methodVisitor -> {
            final Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(816, l0);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitIntInsn(SIPUSH, 393);
            final Label l1 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPLT, l1);
            final Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(817, l2);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 1);
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(818, l1);
            methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "de/exceptionflug/protocolize/items/ItemType", "values", "()[Lde/exceptionflug/protocolize/items/ItemType;", false);
            methodVisitor.visitVarInsn(ASTORE, 4);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitInsn(ARRAYLENGTH);
            methodVisitor.visitVarInsn(ISTORE, 5);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 6);
            final Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitFrame(F_APPEND,3, new Object[] {"[Lde/exceptionflug/protocolize/items/ItemType;", INTEGER, INTEGER}, 0, null);
            methodVisitor.visitVarInsn(ILOAD, 6);
            methodVisitor.visitVarInsn(ILOAD, 5);
            final Label l4 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPGE, l4);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 6);
            methodVisitor.visitInsn(AALOAD);
            methodVisitor.visitVarInsn(ASTORE, 7);
            final Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLineNumber(819, l5);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "de/exceptionflug/protocolize/items/ItemType", "getApplicableMapping", "(I)Lde/exceptionflug/protocolize/items/ItemIDMapping;", false);
            methodVisitor.visitVarInsn(ASTORE, 8);
            final Label l6 = new Label();
            methodVisitor.visitLabel(l6);
            methodVisitor.visitLineNumber(820, l6);
            methodVisitor.visitVarInsn(ALOAD, 8);
            final Label l7 = new Label();
            methodVisitor.visitJumpInsn(IFNULL, l7);
            final Label l8 = new Label();
            methodVisitor.visitLabel(l8);
            methodVisitor.visitLineNumber(821, l8);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitTypeInsn(INSTANCEOF, "de/exceptionflug/protocolize/items/AbstractCustomItemIDMapping");
            final Label l9 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, l9);
            final Label l10 = new Label();
            methodVisitor.visitLabel(l10);
            methodVisitor.visitLineNumber(822, l10);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitTypeInsn(CHECKCAST, "de/exceptionflug/protocolize/items/AbstractCustomItemIDMapping");
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "de/exceptionflug/protocolize/items/AbstractCustomItemIDMapping", "isApplicable", "(Lde/exceptionflug/protocolize/items/ItemStack;III)Z", false);
            methodVisitor.visitJumpInsn(IFEQ, l7);
            final Label l11 = new Label();
            methodVisitor.visitLabel(l11);
            methodVisitor.visitLineNumber(823, l11);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(l9);
            methodVisitor.visitLineNumber(826, l9);
            methodVisitor.visitFrame(F_APPEND,2, new Object[] {"de/exceptionflug/protocolize/items/ItemType", "de/exceptionflug/protocolize/items/ItemIDMapping"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "de/exceptionflug/protocolize/items/ItemIDMapping", "getId", "()I", false);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitJumpInsn(IF_ICMPNE, l7);
            methodVisitor.visitVarInsn(ALOAD, 8);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "de/exceptionflug/protocolize/items/ItemIDMapping", "getData", "()I", false);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitJumpInsn(IF_ICMPNE, l7);
            final Label l12 = new Label();
            methodVisitor.visitLabel(l12);
            methodVisitor.visitLineNumber(827, l12);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(l7);
            methodVisitor.visitLineNumber(818, l7);
            methodVisitor.visitFrame(F_CHOP,2, null, 0, null);
            methodVisitor.visitIincInsn(6, 1);
            methodVisitor.visitJumpInsn(GOTO, l3);
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(832, l4);
            methodVisitor.visitFrame(F_CHOP,3, null, 0, null);
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitInsn(ARETURN);
            final Label l13 = new Label();
            methodVisitor.visitLabel(l13);
            methodVisitor.visitLocalVariable("mapping", "Lde/exceptionflug/protocolize/items/ItemIDMapping;", null, l6, l7, 8);
            methodVisitor.visitLocalVariable("type", "Lde/exceptionflug/protocolize/items/ItemType;", null, l5, l7, 7);
            methodVisitor.visitLocalVariable("id", "I", null, l0, l13, 0);
            methodVisitor.visitLocalVariable("durability", "S", null, l0, l13, 1);
            methodVisitor.visitLocalVariable("protocolVersion", "I", null, l0, l13, 2);
            methodVisitor.visitLocalVariable("stack", "Lde/exceptionflug/protocolize/items/ItemStack;", null, l0, l13, 3);
        });
    }

    private static Method makeGetType0() {
        final Map<String, Class> params = new LinkedHashMap<>();
        params.put("id", int.class);
        params.put("protocolVersion", int.class);
        params.put("stack", ItemStack.class);
        return new Method("getType", ACC_PUBLIC + ACC_STATIC, params, ItemType.class, methodVisitor -> {
            final Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(812, l0);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "de/exceptionflug/protocolize/items/ItemType", "getType", "(ISILde/exceptionflug/protocolize/items/ItemStack;)Lde/exceptionflug/protocolize/items/ItemType;", false);
            methodVisitor.visitInsn(ARETURN);
            final Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("id", "I", null, l0, l1, 0);
            methodVisitor.visitLocalVariable("protocolVersion", "I", null, l0, l1, 1);
            methodVisitor.visitLocalVariable("stack", "Lde/exceptionflug/protocolize/items/ItemStack;", null, l0, l1, 2);
        });
    }

}
