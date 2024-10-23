package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toCValues
import llvm.*

@OptIn(ExperimentalForeignApi::class)
class Type internal constructor(private val ref: LLVMTypeRef?, private val context: Context) {
    val llvmRef: LLVMTypeRef?
        get() = this.ref

    fun constInt(value: Int): LLVMValueRef? {
        return LLVMConstInt(this.ref, value.toULong(), 0)
    }

    fun constInt(value: UInt): LLVMValueRef? {
        return LLVMConstInt(this.ref, value.toULong(), 0)
    }

    fun constInt(value: Long): LLVMValueRef? {
        return LLVMConstInt(this.ref, value.toULong(), 0)
    }

    fun constInt(value: ULong): LLVMValueRef? {
        return LLVMConstInt(this.ref, value, 0)
    }

    val pointer: Type
        get() = Type(LLVMPointerTypeInContext(context.llvmRef, 0U), context)

    companion object {
        fun Function(context: Context, parameterTypes: List<Type>, returnType: Type, vararg: Boolean = false): Type {
            val llvmFunctionType = LLVMFunctionType(
                ReturnType = returnType.llvmRef,
                ParamTypes = parameterTypes.map(Type::llvmRef).toCValues(),
                ParamCount = parameterTypes.size.toUInt(),
                IsVarArg = if (vararg) 1 else 0,
            )

            return Type(llvmFunctionType, context)
        }
    }
}