
package moe.nea.firmament.init;

import me.shedaniel.mm.api.ClassTinkerers;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class HandledScreenRiser extends RiserUtils {
	Intermediary.InterClass Screen = Intermediary.<Screen>intermediaryClass();
	Intermediary.InterClass KeyInput = Intermediary.<KeyInput>intermediaryClass();
	Intermediary.InterClass CharInput = Intermediary.<CharInput>intermediaryClass();
	Intermediary.InterClass HandledScreen = Intermediary.<HandledScreen>intermediaryClass();
	Intermediary.InterMethod mouseScrolled = Intermediary.intermediaryMethod(
		Element::mouseScrolled,
		Intermediary.ofClass(boolean.class),
		Intermediary.ofClass(double.class),
		Intermediary.ofClass(double.class),
		Intermediary.ofClass(double.class),
		Intermediary.ofClass(double.class)
	);
	Intermediary.InterMethod keyReleased = Intermediary.intermediaryMethod(
		Element::keyReleased,
		Intermediary.ofClass(boolean.class),
		KeyInput
	);
	Intermediary.InterMethod charTyped = Intermediary.intermediaryMethod(
		Element::charTyped,
		Intermediary.ofClass(boolean.class),
		CharInput
	);



	@Override
	public void addTinkerers() {
		addTransformation(HandledScreen, this::addMouseScroll, true);
		addTransformation(HandledScreen, this::addKeyReleased, true);
		addTransformation(HandledScreen, this::addCharTyped, true);
	}

	/**
	 * Insert a handler that roughly inserts the following code at the beginning of the instruction list:
	 * <code><pre>
	 * if (insertInvoke(insertLoads)) return true
	 * </pre></code>
	 *
	 * @param node         The method node to prepend the instructions to
	 * @param insertLoads  insert all the loads, including the {@code this} parameter
	 * @param insertInvoke insert the invokevirtual/invokestatic call
	 */
	void insertTrueHandler(MethodNode node,
						   Consumer<InsnList> insertLoads,
						   Consumer<InsnList> insertInvoke) {

		var insns = new InsnList();
		insertLoads.accept(insns);
		insertInvoke.accept(insns);
		// Create jump target (but not insert it yet)
		var jumpIfFalse = new LabelNode();
		// IFEQ (if returned boolean == 0), jump to jumpIfFalse
		insns.add(new JumpInsnNode(Opcodes.IFEQ, jumpIfFalse));
		// LDC 1 (as int, which is what booleans are at runtime)
		insns.add(new LdcInsnNode(1));
		// IRETURN return int on stack (booleans are int at runtime)
		insns.add(new InsnNode(Opcodes.IRETURN));
		insns.add(jumpIfFalse);
		node.instructions.insert(insns);
	}

	void addKeyReleased(ClassNode classNode) {
		addSuperInjector(
			classNode, keyReleased.mapped(), keyReleased.mappedDesc(), "keyReleased_firmament",
			insns -> {
				// ALOAD 0, load this
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				// ALOAD 1, load args
				insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			});
	}

	void addCharTyped(ClassNode classNode) {
		addSuperInjector(
			classNode, charTyped.mapped(), charTyped.mappedDesc(), "charTyped_firmament",
			insns -> {
				// ALOAD 0, load this
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				// ALOAD 1, load args
				insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
			});
	}

	void addSuperInjector(
		ClassNode classNode,
		String name,
		Type desc,
		String firmamentName,
		Consumer<InsnList> loadArgs
	) {
		var keyReleasedNode = findMethod(classNode, name, desc);
		if (keyReleasedNode == null) {
			keyReleasedNode = new MethodNode(
				Modifier.PUBLIC,
				name,
				desc.getDescriptor(),
				null,
				new String[0]
			);
			var insns = keyReleasedNode.instructions;
			loadArgs.accept(insns);
			// INVOKESPECIAL call super method
			insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Screen.mapped().getInternalName(),
				name, desc.getDescriptor()));
			// IRETURN return int on stack (booleans are int at runtime)
			insns.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(keyReleasedNode);
		}
		insertTrueHandler(keyReleasedNode, loadArgs, insns -> {
			// INVOKEVIRTUAL call custom handler
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
				HandledScreen.mapped().getInternalName(),
				firmamentName,
				desc.getDescriptor()));
		});

	}

	void addMouseScroll(ClassNode classNode) {
		addSuperInjector(
			classNode, mouseScrolled.mapped(), mouseScrolled.mappedDesc(), "mouseScrolled_firmament",
			insns -> {
				// ALOAD 0, load this
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				// DLOAD 1-4, load the 4 argument doubles. Note that since doubles are two entries wide we skip 2 each time.
				insns.add(new VarInsnNode(Opcodes.DLOAD, 1));
				insns.add(new VarInsnNode(Opcodes.DLOAD, 3));
				insns.add(new VarInsnNode(Opcodes.DLOAD, 5));
				insns.add(new VarInsnNode(Opcodes.DLOAD, 7));
			});
	}

}
