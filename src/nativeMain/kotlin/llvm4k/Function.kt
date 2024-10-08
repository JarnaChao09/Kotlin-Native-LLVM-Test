package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import llvm.LLVMCountParams
import llvm.LLVMGetParam
import llvm.LLVMValueRef

@OptIn(ExperimentalForeignApi::class)
class Function internal constructor(private val ref: LLVMValueRef?) {
    val llvmRef: LLVMValueRef?
        get() = this.ref

    val basicBlocks: BasicBlockCollection
        get() = BasicBlockCollection(this)

    private fun getParam(index: UInt): LLVMValueRef? {
        return LLVMGetParam(this.ref, index)
    }

    val parameters: List<LLVMValueRef?> = List(LLVMCountParams(this.ref).toInt()) {
        getParam(it.toUInt())
    }
}