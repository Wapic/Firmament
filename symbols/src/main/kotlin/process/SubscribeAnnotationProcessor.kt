package moe.nea.firmament.annotations.process

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.validate
import moe.nea.firmament.annotations.Subscribe

class SubscribeAnnotationProcessor(
    val logger: KSPLogger,
    val codeGenerator: CodeGenerator,
    val sourceSetName: String,
) : SymbolProcessor {
    override fun finish() {
        subscriptions.sort()
        if (subscriptions.isEmpty()) return
        val subscriptionSet = subscriptions.mapTo(mutableSetOf()) { it.parent.containingFile!! }
        val dependencies = Dependencies(
            aggregating = true,
            *subscriptionSet.toTypedArray())
        val generatedFileName = "AllSubscriptions${sourceSetName.replaceFirstChar { it.uppercaseChar() }}"
        val subscriptionsFile =
            codeGenerator
                .createNewFile(dependencies, "moe.nea.firmament.annotations.generated", generatedFileName)
                .bufferedWriter()
        subscriptionsFile.apply {
            appendLine("// This file is @generated by SubscribeAnnotationProcessor")
            appendLine("// Do not edit")
            for (file in subscriptionSet) {
                appendLine("// Dependency: ${file.filePath}")
            }
            appendLine("package moe.nea.firmament.annotations.generated")
            appendLine()
            appendLine("import moe.nea.firmament.events.subscription.*")
            appendLine()
            appendLine("class $generatedFileName : SubscriptionList {")
            appendLine("  override fun provideSubscriptions(addSubscription: (Subscription<*>) -> Unit) {")
            for (subscription in subscriptions) {
                val owner = subscription.parent.qualifiedName!!.asString()
                val method = subscription.child.simpleName.asString()
                val type = subscription.type.declaration.qualifiedName!!.asString()
                appendLine("    addSubscription(Subscription<$type>(")
                appendLine("        ${owner},")
                appendLine("        ${owner}::${method},")
                appendLine("        ${type},")
                appendLine("        \"${method}\"))")
            }
            appendLine("  }")
            appendLine("}")
        }
        subscriptionsFile.close()
        val metaInf = codeGenerator.createNewFileByPath(
            Dependencies(false),
            "META-INF/services/moe.nea.firmament.events.subscription.SubscriptionList", extensionName = "")
            .bufferedWriter()
        metaInf.append("moe.nea.firmament.annotations.generated.")
        metaInf.appendLine(generatedFileName)
        metaInf.close()
    }

    data class Subscription(
        val parent: KSClassDeclaration,
        val child: KSFunctionDeclaration,
        val type: KSType,
    ) : Comparable<Subscription> {
        override fun compareTo(other: Subscription): Int {
            var compare = parent.qualifiedName!!.asString().compareTo(other.parent.qualifiedName!!.asString())
            if (compare != 0) return compare
            compare = other.child.simpleName.asString().compareTo(child.simpleName.asString())
            if (compare != 0) return compare
            compare = other.type.declaration.qualifiedName!!.asString()
                .compareTo(type.declaration.qualifiedName!!.asString())
            if (compare != 0) return compare
            return 0
        }
    }

    val subscriptions = mutableListOf<Subscription>()

    fun processCandidates(list: List<KSAnnotated>) {
        for (element in list) {
            if (element !is KSFunctionDeclaration) {
                logger.error("@Subscribe annotation on a not-function", element)
                continue
            }
            if (element.isAbstract) {
                logger.error("@Subscribe annotation on an abstract function", element)
                continue
            }
            val parent = element.parentDeclaration
            if (parent !is KSClassDeclaration || parent.classKind != ClassKind.OBJECT) {
                logger.error("@Subscribe on a non-object", element)
                continue
            }
            val param = element.parameters.singleOrNull()
            if (param == null) {
                logger.error("@Subscribe annotated functions need to take exactly one parameter", element)
                continue
            }
            val type = param.type.resolve()
            if (type.nullability != Nullability.NOT_NULL) {
                logger.error("@Subscribe annotated functions cannot take a nullable event", element)
                continue
            }
            subscriptions.add(Subscription(parent, element, type))
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val candidates = resolver.getSymbolsWithAnnotation(Subscribe::class.qualifiedName!!).toList()
        val valid = candidates.filter { it.validate() }
        val invalid = candidates.filter { !it.validate() }
        processCandidates(valid)
        return invalid
    }
}

@AutoService(SymbolProcessorProvider::class)
class SubscribeAnnotationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return SubscribeAnnotationProcessor(environment.logger,
                                            environment.codeGenerator,
                                            environment.options["firmament.sourceset"] ?: "main")
    }
}
