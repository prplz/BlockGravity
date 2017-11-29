package io.prplz.blockgravity.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassTransformer implements IClassTransformer {

    private final String HOOKS = "io.prplz.blockgravity.asm.Hooks".replace('.', '/');

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(0);
        classReader.accept(new ClassVisitor(Opcodes.ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String callerName, String callerDesc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, callerName, callerDesc, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM5, mv) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if (opcode == Opcodes.INVOKEVIRTUAL) {
                            if (owner.equals("net/minecraft/block/Block")) {
                                if ((name.equals("onBlockAdded") || name.equals("func_176213_c")) && desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V")) {
                                    System.out.println("Found call to Block.onBlockAdded in " + transformedName);
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "onBlockAdded", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V", false);
                                    return;
                                }
                                if ((name.equals("updateTick") || name.equals("func_180650_b")) && desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V")) {
                                    System.out.println("Found call to Block.updateTick in " + transformedName);
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "updateTick", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", false);
                                    return;
                                }
                                if ((name.equals("neighborChanged") || name.equals("func_189540_a")) && desc.equals("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V")) {
                                    System.out.println("Found call to Block.neighborChanged in " + transformedName);
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "neighborChanged", "(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V", false);
                                    return;
                                }
                            }
                            if ((name.equals("getCollisionBoxes") || name.equals("func_184144_a")) && desc.equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;")) {
                                System.out.println("Found call to World.getCollisionBoxes in " + transformedName);
                                mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "getCollisionBoxes", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;");
                                return;
                            }
                        }
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }

                    @Override
                    public void visitCode() {
                        // workaround bug with EntityFallingBlock landing on fences: Just delete the entity
                        if (transformedName.equals("net.minecraft.entity.item.EntityFallingBlock")) {
                            if ((callerName.equals("onUpdate") || callerName.equals("func_70071_h_")) && callerDesc.equals("()V")) {
                                System.out.println("Found EntityFallingBlock.onUpdate");
                                mv.visitVarInsn(Opcodes.ALOAD, 0);
                                mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "entityFallingBlockOnUpdate", "(Lnet/minecraft/entity/item/EntityFallingBlock;)V");
                            }
                        }
                        super.visitCode();
                    }
                };
            }
        }, 0);
        return classWriter.toByteArray();
    }
}
