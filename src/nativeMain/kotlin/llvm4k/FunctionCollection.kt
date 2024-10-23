package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import llvm.LLVMAddFunction
import llvm.LLVMTypeRef

@OptIn(ExperimentalForeignApi::class)
class FunctionCollection internal constructor(private val mod: Module) {
    private val functions: MutableMap<String, Pair<Type, Function>> = mutableMapOf()

    fun add(name: String, parameterTypes: List<Type>, returnType: Type, vararg: Boolean = false): Function {
        val functionType = Type.Function(this.mod.context, parameterTypes, returnType, vararg)

        val function = LLVMAddFunction(this.mod.llvmRef, name, functionType.llvmRef)

        return Function(function).also {
            this.functions[name] = functionType to it
        }
    }

    operator fun get(name: String): Pair<Type, Function> {
        return this.functions[name]!!
    }
}