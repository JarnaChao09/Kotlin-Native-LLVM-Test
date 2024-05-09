import kotlinx.cinterop.*
import llvm.*

// reference from: https://github.com/AlexPl292/Kaleidoscope-Kotlin-Llvm/blob/master/src/kaleidoscopeMain/kotlin/KaleidoscopeJIT.kt
@OptIn(ExperimentalForeignApi::class)
class KaleidoscopeJIT(context: LLVMContextRef?) {
    var executionEngine: LLVMExecutionEngineRef?
    private val jitModule = LLVMModuleCreateWithNameInContext("jit", context)

    init {
        executionEngine = memScoped {
            val ee = alloc<LLVMExecutionEngineRefVar>()
            val errorPtr = alloc<CPointerVar<ByteVar>>()
            val res = LLVMCreateJITCompilerForModule(ee.ptr, jitModule, 0u, errorPtr.ptr)
            if (res != 0) error("Error in execution engine initialization: ${errorPtr.value?.toKString()}")
            ee.value
        }
    }


    fun addModule(module: LLVMModuleRef?) {
        LLVMAddModule(executionEngine, module)
    }

    fun removeModule(module: LLVMModuleRef?) {
        memScoped {
            val outModule = alloc<LLVMModuleRefVar>()
            val errorPtr = alloc<CPointerVar<ByteVar>>()
            val res = LLVMRemoveModule(executionEngine, module, outModule.ptr, errorPtr.ptr)
            if (res != 0) error("Error in removing module from jit ${errorPtr.value?.toKString()}")
        }
    }

    fun findSymbol(name: String): LLVMValueRef? = memScoped {
        val myFunction = alloc<LLVMValueRefVar>()
        LLVMFindFunction(executionEngine, name, myFunction.ptr)
        myFunction.value
    }
}

@OptIn(ExperimentalForeignApi::class)
fun main() {
    val context = LLVMContextCreate()

    val module = LLVMModuleCreateWithNameInContext("main", context)
    val builder = LLVMCreateBuilderInContext(context)

    val i8Type = LLVMInt8TypeInContext(context)
    val i8TypePtr = LLVMPointerType(i8Type, 0u)
    val i32Type = LLVMInt32TypeInContext(context)

    val printfFunctionType = LLVMFunctionType(
        ReturnType = i32Type,
        ParamTypes = arrayOf(i8TypePtr).toCValues(),
        ParamCount = 1u,
        IsVarArg = 1,
    )

    val printfFunction = LLVMAddFunction(module, "printf", printfFunctionType)

    val mainFunctionType = LLVMFunctionType(
        ReturnType = i32Type,
        ParamTypes = null,
        ParamCount = 0u,
        IsVarArg = 0,
    )

    val mainFunction = LLVMAddFunction(module, "main", mainFunctionType)

    val entry = LLVMAppendBasicBlockInContext(context, mainFunction, Name = "entry")
    LLVMPositionBuilderAtEnd(builder, entry)

    val printfArgs = arrayOf(
        LLVMBuildGlobalStringPtr(builder, "%s\n", "printfString"),
        LLVMBuildGlobalStringPtr(builder, "Hello from LLVM JIT!", "printfString2")
    )

    LLVMBuildCall2(
        builder,
        printfFunctionType,
        printfFunction,
        printfArgs.toCValues(),
        printfArgs.size.toUInt(),
        "printfCall"
    )

    LLVMBuildRet(builder, LLVMConstInt(i32Type, 0u, 0))

    LLVMDumpModule(module)

    LLVMLinkInMCJIT()
    LLVMInitializeNativeTarget()
    LLVMInitializeNativeAsmPrinter()

    val jit = KaleidoscopeJIT(context)

    jit.addModule(module)


    val function = jit.findSymbol("main")

    val ret = LLVMRunFunction(jit.executionEngine, function, 0u, null)

    println("exited with status code ${LLVMGenericValueToInt(ret, 1)}")

    jit.removeModule(module)

    LLVMDisposeBuilder(builder)
    LLVMDisposeModule(module)
    LLVMContextDispose(context)
}
