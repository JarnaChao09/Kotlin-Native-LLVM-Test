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

    fun alloca(type: Type, name: String = ""): LLVMValueRef? {
        return LLVMBuildAlloca(this.ref, type.llvmRef, name)
    }

    fun load(type: Type, pointerValue: LLVMValueRef?, name: String = ""): LLVMValueRef? {
        return LLVMBuildLoad2(this.ref, type.llvmRef, pointerValue, name)
    }

    fun store(value: LLVMValueRef?, pointer: LLVMValueRef?): LLVMValueRef? {
        return LLVMBuildStore(this.ref, value, pointer)
    }

    fun add(left: LLVMValueRef?, right: LLVMValueRef?, name: String = ""): LLVMValueRef? {
        return LLVMBuildAdd(this.ref, left, right, name)
    }

    fun mul(left: LLVMValueRef?, right: LLVMValueRef?, name: String = ""): LLVMValueRef? {
        return LLVMBuildMul(this.ref, left, right, name)
    }

    fun call(functionType: Type, function: LLVMValueRef?, args: Array<LLVMValueRef?>, name: String = ""): LLVMValueRef? {
        return LLVMBuildCall2(this.ref, functionType.llvmRef, function, args.toCValues(), args.size.toUInt(), name)
    }

    fun globalStringPointer(str: String, name: String = ""): LLVMValueRef? {
        return LLVMBuildGlobalStringPtr(this.ref, str, name)
    }

    fun extractValue(value: LLVMValueRef?, index: UInt, name: String = ""): LLVMValueRef? {
        return LLVMBuildExtractValue(this.ref, value, index, name)
    }

    fun gep(type: Type, pointer: LLVMValueRef?, indices: Array<LLVMValueRef?>, name: String = ""): LLVMValueRef? {
        return LLVMBuildGEP2(this.ref, type.llvmRef, pointer, indices.toCValues(), indices.size.toUInt(), name)
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