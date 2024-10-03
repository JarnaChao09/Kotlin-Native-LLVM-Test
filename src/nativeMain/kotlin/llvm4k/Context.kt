package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import llvm.*

@OptIn(ExperimentalForeignApi::class)
class Context internal constructor(private val ref: LLVMContextRef?) {
    val int32: LLVMTypeRef?
        get() = LLVMInt32TypeInContext(this.ref)

    fun newModule(name: String): Module {
        return Module(LLVMModuleCreateWithNameInContext(name, this.ref))
    }
}

@OptIn(ExperimentalForeignApi::class)
class ThreadSafeContext private constructor(private val ref: LLVMOrcThreadSafeContextRef?) {
    private var disposed: Boolean = false

    val llvmRef: LLVMOrcThreadSafeContextRef?
        get() = this.ref

    val context: Context by lazy { Context(LLVMOrcThreadSafeContextGetContext(this.ref)) }

    fun dispose() {
        if (!disposed) {
            disposed = true
            LLVMOrcDisposeThreadSafeContext(this.ref)
        }
    }

    companion object {
        operator fun invoke(): ThreadSafeContext {
            return ThreadSafeContext(LLVMOrcCreateNewThreadSafeContext())
        }
    }
}