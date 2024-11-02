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

    fun alloca(type: Type, name: String = ""): Value {
        return LLVMBuildAlloca(this.ref, type.llvmRef, name)
    }

    fun load(type: Type, pointerValue: Value, name: String = ""): Value {
        return LLVMBuildLoad2(this.ref, type.llvmRef, pointerValue, name)
    }

    fun store(value: Value, pointer: Value): Value {
        return LLVMBuildStore(this.ref, value, pointer)
    }

    fun add(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildAdd(this.ref, left, right, name)
    }
    
    fun sub(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildSub(this.ref, left, right, name)
    }

    fun mul(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildMul(this.ref, left, right, name)
    }

    fun fmul(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildFMul(this.ref, left, right, name)
    }

    fun udiv(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildUDiv(this.ref, left, right, name)
    }

    fun udivExact(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildExactUDiv(this.ref, left, right, name)
    }

    fun sdiv(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildSDiv(this.ref, left, right, name)
    }

    fun sdivExact(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildExactSDiv(this.ref, left, right, name)
    }

    fun fdiv(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildFDiv(this.ref, left, right, name)
    }

    fun urem(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildURem(this.ref, left, right, name)
    }

    fun srem(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildSRem(this.ref, left, right, name)
    }

    fun frem(left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildFRem(this.ref, left, right, name)
    }

    fun icmp(predicate: LLVMIntPredicate, left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildICmp(this.ref, predicate, left, right, name)
    }

    fun fcmp(predicate: LLVMRealPredicate, left: Value, right: Value, name: String = ""): Value {
        return LLVMBuildFCmp(this.ref, predicate, left, right, name)
    }

    fun call(functionType: Type, function: Value, args: Array<Value>, name: String = ""): Value {
        return LLVMBuildCall2(this.ref, functionType.llvmRef, function, args.toCValues(), args.size.toUInt(), name)
    }

    fun globalStringPointer(str: String, name: String = ""): Value {
        return LLVMBuildGlobalStringPtr(this.ref, str, name)
    }

    fun extractValue(value: Value, index: UInt, name: String = ""): Value {
        return LLVMBuildExtractValue(this.ref, value, index, name)
    }

    fun gep(type: Type, pointer: Value, indices: Array<Value>, name: String = ""): Value {
        return LLVMBuildGEP2(this.ref, type.llvmRef, pointer, indices.toCValues(), indices.size.toUInt(), name)
    }

    fun ret(value: Value): Value {
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