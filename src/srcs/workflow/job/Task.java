package srcs.workflow.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.TYPE)
public @interface Task {
    String value();
}
