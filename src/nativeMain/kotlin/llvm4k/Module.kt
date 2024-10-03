package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import llvm.LLVMModuleRef
import llvm.LLVMOrcCreateNewThreadSafeModule
import llvm.LLVMOrcThreadSafeModuleRef

@OptIn(ExperimentalForeignApi::class)
class Module internal constructor(private val ref: LLVMModuleRef?) {
    val llvmRef: LLVMModuleRef?
        get() = this.ref

    val functions: FunctionCollection by lazy { FunctionCollection(this) }

    companion object
}

@OptIn(ExperimentalForeignApi::class)
class ThreadSafeModule internal constructor(private val ref: LLVMOrcThreadSafeModuleRef?) {
    val llvmRef: LLVMOrcThreadSafeModuleRef?
        get() = this.ref

    companion object {
        operator fun invoke(module: Module, threadSafeContext: ThreadSafeContext): ThreadSafeModule {
            return ThreadSafeModule(LLVMOrcCreateNewThreadSafeModule(module.llvmRef, threadSafeContext.llvmRef))
        }
    }
}