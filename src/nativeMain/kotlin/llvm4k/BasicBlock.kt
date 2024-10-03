package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import llvm.LLVMBasicBlockRef

@OptIn(ExperimentalForeignApi::class)
class BasicBlock internal constructor(private val ref: LLVMBasicBlockRef?) {
    val llvmRef: LLVMBasicBlockRef?
        get() = this.ref
}