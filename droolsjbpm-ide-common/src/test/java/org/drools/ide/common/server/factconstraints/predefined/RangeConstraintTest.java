/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.ide.common.server.factconstraints.predefined;

import java.util.Collection;

import org.drools.ide.common.client.factconstraints.ConstraintConfiguration;
import org.drools.ide.common.client.factconstraints.ValidationResult;
import org.drools.ide.common.client.factconstraints.config.SimpleConstraintConfigurationImpl;
import org.drools.ide.common.server.factconstraints.Constraint;
import org.drools.verifier.Verifier;
import org.drools.verifier.VerifierConfiguration;
import org.drools.verifier.VerifierConfigurationImpl;
import org.drools.verifier.VerifierError;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.drools.verifier.data.VerifierReport;
import org.drools.verifier.report.components.Severity;
import org.drools.verifier.report.components.VerifierMessageBase;
import org.junit.Before;
import org.junit.Test;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;

import static org.junit.Assert.*;

public class RangeConstraintTest {

    private Constraint cons;
    private ConstraintConfiguration conf;

    @Before
    public void setup() {
        cons = new RangeConstraint();
        conf = new SimpleConstraintConfigurationImpl();
        conf.setFactType("Person");
        conf.setFieldName("age");
    }

    //@Test
    @Test
    public void testValidConstraint() {

        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MIN, "-0.5");
        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MAX, "100");

        ValidationResult result = cons.validate(12, conf);
        assertTrue(result.isSuccess());

        result = cons.validate(new Integer("12"), conf);
        assertTrue(result.isSuccess());

        result = cons.validate("12", conf);
        assertTrue(result.isSuccess());

        result = cons.validate(0.6, conf);
        assertTrue(result.isSuccess());

        result = cons.validate(new Float("-0.3"), conf);
        assertTrue(result.isSuccess());

        result = cons.validate("90.76", conf);
        assertTrue(result.isSuccess());

    }

    //@Test
    @Test
    public void testInvalidConstraint() {

        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MIN, "-0.5");
        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MAX, "100");

        ValidationResult result = cons.validate(new Object(), conf);
        assertFalse(result.isSuccess());
        System.out.println("Message: " + result.getMessage());

        result = cons.validate(null, conf);
        assertFalse(result.isSuccess());
        System.out.println("Message: " + result.getMessage());

        result = cons.validate("", conf);
        assertFalse(result.isSuccess());
        System.out.println("Message: " + result.getMessage());

        result = cons.validate("ABC", conf);
        assertFalse(result.isSuccess());
        System.out.println("Message: " + result.getMessage());

        result = cons.validate(new Long("-100"), conf);
        assertFalse(result.isSuccess());
        System.out.println("Message: " + result.getMessage());

        result = cons.validate(-0.5, conf);
        assertFalse(result.isSuccess());
        System.out.println("Message: " + result.getMessage());

        result = cons.validate(100, conf);
        assertFalse(result.isSuccess());
        System.out.println("Message: " + result.getMessage());


    }

    //@Test
    @Test
    public void testUsingVerifier() {

        //age constraint
        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MIN, "0");
        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MAX, "120");
        System.out.println("Validation Rule:\n" + cons.getVerifierRule(conf) + "\n\n");

        //salary constraint
        ConstraintConfiguration salaryCons = new SimpleConstraintConfigurationImpl();
        salaryCons.setFactType("Person");
        salaryCons.setFieldName("salary");
        salaryCons.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MIN, "0");
        salaryCons.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MAX, "1000.6");

        System.out.println("Validation Rule:\n" + cons.getVerifierRule(salaryCons) + "\n\n");


        String ruleToVerify = "";
        int fail = 0;

        //OK
        ruleToVerify += "package org.kie.factconstraint.test\n\n";
        ruleToVerify += "import org.kie.factconstraint.model.*\n";
        ruleToVerify += "rule \"rule1\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 10)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n\n";

        //FAIL - 1
        ruleToVerify += "rule \"rule2\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == -5)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n\n";
        fail++;

        //OK
        ruleToVerify += "rule \"rule3\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 100)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";

        //OK
        ruleToVerify += "rule \"rule4\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(salary == 100)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";

        //OK
        ruleToVerify += "rule \"rule5\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(salary == 89.67)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";

        //FAIL - 2
        ruleToVerify += "rule \"rule6\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(salary == 1000.7)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";
        fail++;

        //FAIL - 3
        ruleToVerify += "rule \"rule7\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(salary == 1024)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";
        fail++;

        //OK
        ruleToVerify += "rule \"rule8\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 45, salary == 1000)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";

        //FAIL: age - 4
        ruleToVerify += "rule \"rule9\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 40, salary == 1011)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";
        fail++;

        //FAIL salary - 5
        ruleToVerify += "rule \"rule10\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 43, salary == 1007)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";
        fail++;

        //FAIL both (creates 2 warnings) - 6,7
        ruleToVerify += "rule \"rule11\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 403, salary == 1008)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";
        fail += 2;

        //FAIL both (creates 2 warnings) - 8,9
        ruleToVerify += "rule \"rule12\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 404, salary == -0.679)\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n";
        fail += 2;

        VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();

        VerifierConfiguration vconf = new VerifierConfigurationImpl();

        vconf.getVerifyingResources().put(ResourceFactory.newByteArrayResource(cons.getVerifierRule(this.conf).getBytes()), ResourceType.DRL);
        vconf.getVerifyingResources().put(ResourceFactory.newByteArrayResource(cons.getVerifierRule(salaryCons).getBytes()), ResourceType.DRL);

        Verifier verifier = vBuilder.newVerifier(vconf);

        verifier.addResourcesToVerify(ResourceFactory.newByteArrayResource(ruleToVerify.getBytes()),
                ResourceType.DRL);

        if (verifier.hasErrors()) {
            for (VerifierError error : verifier.getErrors()) {
                System.out.println(error.getMessage());
            }
            throw new RuntimeException("Error building verifier");
        }

        assertFalse(verifier.hasErrors());

        boolean noProblems = verifier.fireAnalysis();
        assertTrue(noProblems);

        VerifierReport result = verifier.getResult();

        Collection<VerifierMessageBase> warnings = result.getBySeverity(Severity.ERROR);

        System.out.println(warnings);

        assertEquals(fail, warnings.size());
        verifier.dispose();
    }

    @Test
    public void testNestedPatternsUsingVerifier() {

        System.out.println("\n\n\n\ntestNestedPatternsUsingVerifier\n");

        //age constraint
        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MIN, "0");
        conf.setArgumentValue(RangeConstraint.RANGE_CONSTRAINT_MAX, "120");
        System.out.println("Validation Rule:\n" + cons.getVerifierRule(conf) + "\n\n");

        String ruleToVerify = "";
        int fail = 0;

        //OK
        ruleToVerify += "package org.kie.factconstraint.test\n\n";
        ruleToVerify += "import org.kie.factconstraint.model.*\n";
//        ruleToVerify += "rule \"rule1\"\n";
//        ruleToVerify += "   when\n";
//        ruleToVerify += "       java.util.List() from collect(Person(age == 10))\n";
//        ruleToVerify += "   then\n";
//        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
//        ruleToVerify += "end\n\n";

        //FAIL - 1
        ruleToVerify += "rule \"rule2\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       java.util.List() from collect(Person(age == 10))\n";
        ruleToVerify += "       java.util.List() from collect(Person(age == 130))\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n\n";
        fail++;

        //FAIL - 2
        ruleToVerify += "rule \"rule3\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 10)\n";
        ruleToVerify += "       Person(age == 20)\n";
        ruleToVerify += "       exists (Person (age == 130))\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n\n";
        fail++;

        ruleToVerify += "rule \"rule4\"\n";
        ruleToVerify += "   when\n";
        ruleToVerify += "       Person(age == 10)\n";
        ruleToVerify += "       exists (Person (age == 30) or Person (age == 130))\n";
        ruleToVerify += "   then\n";
        ruleToVerify += "       System.out.println(\"Rule fired\");\n";
        ruleToVerify += "end\n\n";
        fail++;


        VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();

        VerifierConfiguration vconf = new VerifierConfigurationImpl();

        vconf.getVerifyingResources().put(ResourceFactory.newByteArrayResource(cons.getVerifierRule(this.conf).getBytes()), ResourceType.DRL);

        Verifier verifier = vBuilder.newVerifier(vconf);

        verifier.addResourcesToVerify(ResourceFactory.newByteArrayResource(ruleToVerify.getBytes()),
                ResourceType.DRL);

        if (verifier.hasErrors()) {
            for (VerifierError error : verifier.getErrors()) {
                System.out.println(error.getMessage());
            }
            throw new RuntimeException("Error building verifier");
        }

        assertFalse(verifier.hasErrors());

        boolean noProblems = verifier.fireAnalysis();
        assertTrue(noProblems);

        VerifierReport result = verifier.getResult();

        Collection<VerifierMessageBase> errors = result.getBySeverity(Severity.ERROR);

        System.out.println(errors);

        assertEquals(fail, errors.size());

//        System.out.println("\nOrders:");
//        for (VerifierMessageBase message : errors) {
//            if (message.getFaulty() instanceof PatternComponent) {
//                int rootPatternOrderNumber = this.getRootPatternOrderNumber((PatternComponent) message.getFaulty());
//                System.out.println(((PatternComponent) message.getFaulty()).getPath()+". Order= "+rootPatternOrderNumber);
//            }
//        }

        verifier.dispose();
    }

//    private int getRootPatternOrderNumber(RuleComponent pattern){
//        if (pattern.getParentPatternComponent() == null){
//            return (pattern instanceof PatternComponent)?((PatternComponent)pattern).getPatternOrderNumber():pattern.getOrderNumber();
//        }else{
//            return getRootPatternOrderNumber(pattern.getParentPatternComponent());
//        }
//    }
}
