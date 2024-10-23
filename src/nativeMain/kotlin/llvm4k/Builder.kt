package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toCValues
import llvm.*

@OptIn(ExperimentalForeignApi::class)
class Builder internal constructor(private val ref: LLVMBuilderRef?) {
    private var disposed: Boolean = false

    fun positionAtEnd(block: BasicBlock) {
        LLVMPositionBuilderAtEnd(this.ref, block.llvmRef)
    }

    fun add(left: LLVMValueRef?, right: LLVMValueRef?, name: String = ""): LLVMValueRef? {
        return LLVMBuildAdd(this.ref, left, right, name)
    }

    fun call(functionType: Type, function: LLVMValueRef?, args: Array<LLVMValueRef?>, name: String = ""): LLVMValueRef? {
        return LLVMBuildCall2(this.ref, functionType.llvmRef, function, args.toCValues(), args.size.toUInt(), name)
    }

    fun globalStringPointer(str: String, name: String = ""): LLVMValueRef? {
        return LLVMBuildGlobalStringPtr(this.ref, str, name)
    }

    fun ret(value: LLVMValueRef?): LLVMValueRef? {
        return LLVMBuildRet(this.ref, value)
    }

    fun dispose() {
        if (!disposed) {
            disposed = true
            LLVMDisposeBuilder(this.ref)
        }
    }

    companion object {
        operator fun invoke(context: LLVMContextRef?): Builder {
            return Builder(LLVMCreateBuilderInContext(context))
        }
    }
}