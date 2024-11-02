package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import llvm.LLVMValueRef

@OptIn(ExperimentalForeignApi::class)
typealias Value = LLVMValueRef?