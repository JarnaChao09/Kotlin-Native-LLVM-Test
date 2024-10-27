package llvm4k

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toCValues
import llvm.*

@OptIn(ExperimentalForeignApi::class)
class Context internal constructor(private val ref: LLVMContextRef?) {
    val llvmRef: LLVMContextRef?
        get() = this.ref

    val int8: Type
        get() = Type(LLVMInt8TypeInContext(this.ref), this)

    val int32: Type
        get() = Type(LLVMInt32TypeInContext(this.ref), this)

    fun struct(elementTypes: Array<Type>, name: String? = null, packed: Boolean = false): Type {
        return Type(LLVMStructTypeInContext(this.ref, elementTypes.map(Type::llvmRef).toCValues(), elementTypes.size.toUInt(), if (packed) 1 else 0), this)
    }

    fun newModule(name: String): Module {
        return Module(LLVMModuleCreateWithNameInContext(name, this.ref), this)
    }

    fun module(name: String, block: Module.() -> Unit): Module {
        return newModule(name).apply(block)
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

fun <R> threadSafeContext(block: ThreadSafeContext.() -> R): R {
    val tsc = ThreadSafeContext()

    // update to use(?)
    // or wrap in try {} finally {}
    return tsc.block().also { tsc.dispose() }
}