import kotlinx.cinterop.*
import llvm.*
import llvm4k.ThreadSafeContext
import llvm4k.ThreadSafeModule
import platform.posix.int32_t

/**
 * Reference Implementation from: https://github.com/llvm/llvm-project/blob/main/llvm/examples/OrcV2Examples/OrcV2CBindingsBasicUsage/OrcV2CBindingsBasicUsage.c
 */

object LLVMShutdown : Exception()

object JITCleanUp : Exception()

@OptIn(ExperimentalForeignApi::class)
fun handleLLVMError(error: LLVMErrorRef?) {
    val llvmErrorMessage = LLVMGetErrorMessage(error)
    println("Error: ${llvmErrorMessage?.toKString() ?: "No Error String given"}")
    LLVMDisposeErrorMessage(llvmErrorMessage)
}

// @OptIn(ExperimentalForeignApi::class)
// fun createDemoModule(): LLVMOrcThreadSafeModuleRef {
//     val threadSafeCtx = LLVMOrcCreateNewThreadSafeContext()
//
//     val ctx = LLVMOrcThreadSafeContextGetContext(threadSafeCtx)
//
//     val module = LLVMModuleCreateWithNameInContext("demo", ctx)
//
//     // demo_main
//     // val printfFunctionType = LLVMFunctionType(
//     //     ReturnType = LLVMInt32Type(),
//     //     ParamTypes = arrayOf(LLVMPointerType(LLVMInt8Type(), 0u)).toCValues(),
//     //     ParamCount = 1u,
//     //     IsVarArg = 1,
//     // )
//     //
//     // val printfFunction = LLVMAddFunction(module, "printf", printfFunctionType)
//     //
//     // val mainFunctionType = LLVMFunctionType(
//     //     ReturnType = LLVMInt32Type(),
//     //     ParamTypes = null,
//     //     ParamCount = 0u,
//     //     IsVarArg = 0,
//     // )
//     //
//     // val mainFunction = LLVMAddFunction(module, "demo_main", mainFunctionType)
//     //
//     // val entry = LLVMAppendBasicBlock(mainFunction, Name = "entry")
//     //
//     // val builder = LLVMCreateBuilder()
//     // LLVMPositionBuilderAtEnd(builder, entry)
//     //
//     // val printfArgs = arrayOf(
//     //     LLVMBuildGlobalStringPtr(builder, "%s\n", "printfString"),
//     //     LLVMBuildGlobalStringPtr(builder, "Hello from LLVM JIT!", "printfString2"),
//     // )
//     //
//     // LLVMBuildCall2(
//     //     builder,
//     //     printfFunctionType,
//     //     printfFunction,
//     //     printfArgs.toCValues(),
//     //     printfArgs.size.toUInt(),
//     //     "printfCall"
//     // )
//     //
//     // LLVMBuildRet(builder, LLVMConstInt(LLVMInt32Type(), 0u, 0))
//
//     // sum
//     val paramTypes = arrayOf(LLVMInt32Type(), LLVMInt32Type())
//     val sumFunctionType = LLVMFunctionType(
//         ReturnType = LLVMInt32Type(),
//         ParamTypes = paramTypes.toCValues(),
//         ParamCount = 2u,
//         IsVarArg = 0,
//     )
//     val sumFunction = LLVMAddFunction(module, "sum", sumFunctionType)
//
//     val entryBasicBlock = LLVMAppendBasicBlock(sumFunction, "entry")
//
//     val builder = LLVMCreateBuilder()
//     LLVMPositionBuilderAtEnd(builder, entryBasicBlock)
//
//     val sumFirstArg = LLVMGetParam(sumFunction, 0u)
//     val sumSecondArg = LLVMGetParam(sumFunction, 1u)
//
//     val result = LLVMBuildAdd(builder, sumFirstArg, sumSecondArg, "result")
//
//     LLVMBuildRet(builder, result)
//
//     LLVMDisposeBuilder(builder)
//
//     val threadSafeModule = LLVMOrcCreateNewThreadSafeModule(module, threadSafeCtx)
//
//     LLVMOrcDisposeThreadSafeContext(threadSafeCtx)
//
//     return threadSafeModule!!
// }

@OptIn(ExperimentalForeignApi::class)
fun createDemoModule(): ThreadSafeModule {
    val threadSafeCtx = ThreadSafeContext()

    val ctx = threadSafeCtx.context

    val module = ctx.newModule("demo")

    val paramTypes = listOf(ctx.int32, ctx.int32)

    val sumFunction = module.functions.add("sum", paramTypes, ctx.int32)

    sumFunction.basicBlocks.append("entry") {
        val sumFirstArg = it.getParam(0u)
        val sumSecondArg = it.getParam(1u)

        val result = add(sumFirstArg, sumSecondArg, "result")

        ret(result)
    }

    val threadSafeModule = ThreadSafeModule(module, threadSafeCtx)

    threadSafeCtx.dispose()

    return threadSafeModule
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

                val sumAddress = alloc<LLVMOrcExecutorAddressVar>()

                val error3 = alloc<LLVMErrorRefVar>()

                error3.value = LLVMOrcLLJITLookup(jit.value, sumAddress.ptr, "sum")

                println("looked up address of main function")

                if (error3.value != null) {
                    handleLLVMError(error3.value)

                    throw JITCleanUp
                }

                println("sum address found at ${sumAddress.value}")

                val sum = sumAddress.value.toLong().toCPointer<CFunction<(int32_t, int32_t) -> int32_t>>()!!

                println("reinterpreted sum function")

                val result = sum.invoke(1, 2)

                println("1 + 2 = $result")
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
