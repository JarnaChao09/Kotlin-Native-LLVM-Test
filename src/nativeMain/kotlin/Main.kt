import kotlinx.cinterop.*
import llvm.*
import llvm4k.ThreadSafeModule
import llvm4k.threadSafeContext
import platform.posix.int32_t

/**
 * Initial Reference Implementation from: https://github.com/llvm/llvm-project/blob/main/llvm/examples/OrcV2Examples/OrcV2CBindingsBasicUsage/OrcV2CBindingsBasicUsage.c
 */

object LLVMShutdown : Exception()

object JITCleanUp : Exception()

@OptIn(ExperimentalForeignApi::class)
fun handleLLVMError(error: LLVMErrorRef?) {
    val llvmErrorMessage = LLVMGetErrorMessage(error)
    println("Error: ${llvmErrorMessage?.toKString() ?: "No Error String given"}")
    LLVMDisposeErrorMessage(llvmErrorMessage)
}

@OptIn(ExperimentalForeignApi::class)
fun createDemoModule(): ThreadSafeModule = threadSafeContext {
    val module = context.module("demo") {
        function("printf", listOf(context.int8.pointer), context.int32, vararg = true)

        function("sum", listOf(context.int32, context.int32), context.int32) {
            basicBlocks.append("entry") {
                val sumFirstArg = it.parameters[0]
                val sumSecondArg = it.parameters[1]

                val ret = alloca(context.int32)

                val addResult = add(sumFirstArg, sumSecondArg)

                store(addResult, ret)

                val returnValue = load(context.int32, ret)

                ret(returnValue)
            }
        }

        function("main", listOf(context.int32, context.int32), context.int32) {
            basicBlocks.append("entry") {
                val arg1 = it.parameters[0]
                val arg2 = it.parameters[1]

                val (sumFunctionType, sumFunction) = functions["sum"]
                val result = call(sumFunctionType, sumFunction.llvmRef, arrayOf(arg1, arg2))

                val (printFunctionType, printFunction) = functions["printf"]
                val printfString = globalStringPointer("sum of %d + %d = %d\n")
                call(printFunctionType, printFunction.llvmRef, arrayOf(printfString, arg1, arg2, result))

                ret(context.int32.constInt(0))
            }
        }
    }

    LLVMDumpModule(module.llvmRef)

    val threadSafeModule = ThreadSafeModule(module, this)

    threadSafeModule
}

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    memScoped {
        val major = alloc<UIntVar>()
        val minor = alloc<UIntVar>()
        val patch = alloc<UIntVar>()

        LLVMGetVersion(major.ptr, minor.ptr, patch.ptr)

        println("LLVM Version ${major.value}.${minor.value}.${patch.value}")
    }

    LLVMInitializeNativeTarget()

    println("initialized native target")

    LLVMInitializeNativeAsmPrinter()

    println("initialized native asm printer")

    memScoped {
        try {
            val jit = alloc<LLVMOrcLLJITRefVar>()

            val error = alloc<LLVMErrorRefVar>()

            error.value = LLVMOrcCreateLLJIT(jit.ptr, null)

            println("created LL JIT")

            if (error.value != null) {
                handleLLVMError(error.value)

                throw LLVMShutdown
            }

            try {
                val module = createDemoModule().llvmRef

                println("created demo module")

                val jitdylib = LLVMOrcLLJITGetMainJITDylib(jit.value)!!

                println("retrieved JIT Dylib")

                val error2 = alloc<LLVMErrorRefVar>()

                error2.value = LLVMOrcLLJITAddLLVMIRModule(jit.value, jitdylib, module)

                println("added LLVM IR Module")

                if (error2.value != null) {
                    LLVMOrcDisposeThreadSafeModule(module)
                    handleLLVMError(error2.value)

                    throw JITCleanUp
                }

                val mainAddress = alloc<LLVMOrcExecutorAddressVar>()

                val error3 = alloc<LLVMErrorRefVar>()

                error3.value = LLVMOrcLLJITLookup(jit.value, mainAddress.ptr, "main")

                println("looked up address of main function")

                if (error3.value != null) {
                    handleLLVMError(error3.value)

                    throw JITCleanUp
                }

                println("main address found at ${mainAddress.value}")

                val mainFunc = mainAddress.value.toLong().toCPointer<CFunction<(int32_t, int32_t) -> int32_t>>()!!

                println("reinterpreted main function")

                println("x = ")
                val p1 = readln().toInt()

                println("y = ")
                val p2 = readln().toInt()

                val result = mainFunc.invoke(p1, p2)

                println("main exited with code $result")
            } catch (_: JITCleanUp) {
            } finally {
                val cleanUpError = alloc<LLVMErrorRefVar>()

                cleanUpError.value = LLVMOrcDisposeLLJIT(jit.value)

                if (cleanUpError.value != null) {
                    handleLLVMError(cleanUpError.value)
                }
            }
        } catch (_: LLVMShutdown) {
        } finally {
            LLVMShutdown()
        }
    }
}
