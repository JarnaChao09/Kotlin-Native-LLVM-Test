package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toCValues
import llvm.LLVMAddFunction
import llvm.LLVMFunctionType
import llvm.LLVMTypeRef

@OptIn(ExperimentalForeignApi::class)
class FunctionCollection internal constructor(private val mod: Module) {
    fun add(name: String, parameterTypes: List<LLVMTypeRef?>, returnType: LLVMTypeRef?, vararg: Boolean = false): Function {
        val functionType = LLVMFunctionType(
            ReturnType = returnType,
            ParamTypes = parameterTypes.toCValues(),
            ParamCount = parameterTypes.size.toUInt(),
            IsVarArg = if (vararg) 1 else 0,
        )

        val function = LLVMAddFunction(this.mod.llvmRef, name, functionType)

        return Function(function)
    }
}