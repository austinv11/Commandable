package commandable.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class TestProcessor {

    public static final String TEST_SERVICE = "package com.test;\npublic interface TestService {}";
    public static final String TEST_IMPL = "package com.test.impl;\nimport com.test.TestService;\nimport commandable.annotations.WireService;\n@WireService(TestService.class) public class TestServiceImpl {}";

    @Test
    public void testProcessor() {
        Compilation compilation = javac()
                .withProcessors(new CommandableProcessor())
                .compile(JavaFileObjects.forSourceString("com.test.TestService", TEST_SERVICE),
                        JavaFileObjects.forSourceString("com.test.impl.TestServiceImpl", TEST_IMPL));
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/com.test.TestService");
    }
}
