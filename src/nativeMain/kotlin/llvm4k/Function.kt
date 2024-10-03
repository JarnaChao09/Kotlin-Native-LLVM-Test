package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toCValues
import llvm.LLVMCountParams
import llvm.LLVMGetParam
import llvm.LLVMGetParams
import llvm.LLVMValueRef

@OptIn(ExperimentalForeignApi::class)
class Function internal constructor(private val ref: LLVMValueRef?) {
    val llvmRef: LLVMValueRef?
        get() = this.ref

    val basicBlocks: BasicBlockCollection
        get() = BasicBlockCollection(this)

    fun getParam(index: UInt): LLVMValueRef? {
        return LLVMGetParam(this.ref, index)
    }

    val parameters: List<LLVMValueRef?>
        get() {
            val paramSize = LLVMCountParams(this.ref)
            val ret = List<LLVMValueRef?>(paramSize.toInt()) { null }
            LLVMGetParams(this.ref, ret.toCValues())

            return ret
        }
}