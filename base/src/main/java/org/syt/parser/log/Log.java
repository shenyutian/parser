package org.syt.parser.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description timber 5.1
 * date 2023/10/20 15:53
 * author by zhulei
 */
public final class Log {

    public static final int ASSERT = 7;
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int INFO = 4;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;

    public static final Forest Forest = new Forest();

    private static final ArrayList<Tree> trees = new ArrayList<>();

    private static volatile Tree[] treeArray = new Tree[0];

    public static void v(String message, Object... args) {
        Forest.v(message, args);
    }


    public static void v(Throwable t, String message, Object... args) {
        Forest.v(t, message, args);
    }


    public static void v(Throwable t) {
        Forest.v(t);
    }

    public static void d(Object message) {
        Forest.d(message.toString());
    }

    public static void d(String message, Object... args) {
        Forest.d(message, args);
    }


    public static void d(Throwable t, String message, Object... args) {
        Forest.d(t, message, args);
    }


    public static void d(Throwable t) {
        Forest.d(t);
    }


    public static void i(String message, Object... args) {
        Forest.i(message, args);
    }


    public static void i(Throwable t, String message, Object... args) {
        Forest.i(t, message, args);
    }


    public static void i(Throwable t) {
        Forest.i(t);
    }


    public static void w(String message, Object... args) {
        Forest.w(message, args);
    }


    public static void w(Throwable t, String message, Object... args) {
        Forest.w(t, message, args);
    }


    public static void w(Throwable t) {
        Forest.w(t);
    }


    public static void e(String message, Object... args) {
        Forest.e(message, args);
    }


    public static void e(Throwable t, String message, Object... args) {
        Forest.e(t, message, args);
    }


    public static void e(Throwable t) {
        Forest.e(t);
    }

    public static void s(String message, Object... args) {
        Forest.s(message, args);
    }


    public static void s(Throwable t, String message, Object... args) {
        Forest.s(t, message, args);
    }


    public static void s(Throwable t) {
        Forest.s(t);
    }


    public static void wtf(String message, Object... args) {
        Forest.wtf(message, args);
    }


    public static void wtf(Throwable t, String message, Object... args) {
        Forest.wtf(t, message, args);
    }


    public static void wtf(Throwable t) {
        Forest.wtf(t);
    }


    public static void log(int priority, String message, Object... args) {
        Forest.log(priority, message, args);
    }


    public static void log(int priority, Throwable t, String message, Object... args) {
        Forest.log(priority, t, message, args);
    }


    public static void log(int priority, Throwable t) {
        Forest.log(priority, t);
    }


    public static Tree asTree() {
        return Forest.asTree();
    }


    public static final Tree tag(String tag) {
        return Forest.tag(tag);
    }


    public static final void plant(Tree tree) {
        Forest.plant(tree);
    }


    public static final void plant(Tree... trees2) {
        Forest.plant(trees2);
    }


    public static final void uproot(Tree tree) {
        Forest.uproot(tree);
    }


    public static final void uprootAll() {
        Forest.uprootAll();
    }


    public static final List<Tree> forest() {
        return Forest.forest();
    }


    public static final int treeCount() {
        return Forest.treeCount();
    }

    private Log() {
        throw new AssertionError();
    }

    public static abstract class Tree {

        private final ThreadLocal<String> explicitTag = new ThreadLocal<>();

        protected abstract void log(int i, String str, String str2, Throwable th);

        public final /* synthetic */ ThreadLocal getExplicitTag$outer_casing_debug() {
            return this.explicitTag;
        }

        public String getTag() {
            String tag = this.explicitTag.get();
            if (tag != null) {
                this.explicitTag.remove();
            }
            return tag;
        }

        public void v(String message, Object... args) {
            prepareLog(2, null, message, args);
        }

        public void v(Throwable t, String message, Object... args) {

            prepareLog(2, t, message, args);
        }

        public void v(Throwable t) {
            prepareLog(2, t, null, new Object[0]);
        }

        public void d(String message, Object... args) {

            prepareLog(3, null, message, args);
        }

        public void d(Throwable t, String message, Object... args) {

            prepareLog(3, t, message, args);
        }

        public void d(Throwable t) {
            prepareLog(3, t, null, new Object[0]);
        }

        public void i(String message, Object... args) {

            prepareLog(4, null, message, args);
        }

        public void i(Throwable t, String message, Object... args) {

            prepareLog(4, t, message, args);
        }

        public void i(Throwable t) {
            prepareLog(4, t, null, new Object[0]);
        }

        public void w(String message, Object... args) {

            prepareLog(5, null, message, args);
        }

        public void w(Throwable t, String message, Object... args) {

            prepareLog(5, t, message, args);
        }

        public void w(Throwable t) {
            prepareLog(5, t, null, new Object[0]);
        }

        public void e(String message, Object... args) {

            prepareLog(6, null, message, args);
        }

        public void e(Throwable t, String message, Object... args) {

            prepareLog(6, t, message, args);
        }

        public void e(Throwable t) {
            prepareLog(6, t, null, new Object[0]);
        }

        public void s(String message, Object... args) {

            prepareLog(8, null, message, args);
        }

        public void s(Throwable t, String message, Object... args) {

            prepareLog(8, t, message, args);
        }

        public void s(Throwable t) {
            prepareLog(8, t, null, new Object[0]);
        }


        public void wtf(String message, Object... args) {
            prepareLog(7, null, message, args);
        }

        public void wtf(Throwable t, String message, Object... args) {

            prepareLog(7, t, message, args);
        }

        public void wtf(Throwable t) {
            prepareLog(7, t, null, new Object[0]);
        }

        public void log(int priority, String message, Object... args) {

            prepareLog(priority, null, message, args);
        }

        public void log(int priority, Throwable t, String message, Object... args) {

            prepareLog(priority, t, message, args);
        }

        public void log(int priority, Throwable t) {
            prepareLog(priority, t, null, new Object[0]);
        }

        protected boolean isLoggable(int priority) {
            return true;
        }

        protected boolean isLoggable(String tag, int priority) {
            return isLoggable(priority);
        }

        private final void prepareLog(int priority, Throwable t, String message, Object... args) {
            String tag = getTag();
            if (!isLoggable(tag, priority)) {
                return;
            }
            String message2 = message;
            if (message2 == null || message2.length() == 0) {
                if (t == null) {
                    return;
                }
                message2 = getStackTraceString(t);
            } else {
                if (!(args != null && args.length == 0)) {
                    message2 = formatMessage(message2, args);
                }
                if (t != null) {
                    message2 = message2 + '\n' + getStackTraceString(t);
                }
            }
            log(priority, tag, message2, t);
        }


        protected String formatMessage(String message, Object[] args) {
            return String.format(message, args);
        }

        private final String getStackTraceString(Throwable t) {
            StringWriter sw = new StringWriter(256);
            PrintWriter pw = new PrintWriter((Writer) sw, false);
            t.printStackTrace(pw);
            pw.flush();
            String stringWriter = sw.toString();
            return stringWriter;
        }
    }

    public static class DebugTree extends Tree {
        private final List<String> fqcnIgnore = Arrays.asList(Log.class.getName(), Forest.class.getName(), Tree.class.getName(), DebugTree.class.getName());
        private static final int MAX_LOG_LENGTH = 4000;
        private static final int MAX_TAG_LENGTH = 30;
        private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");
        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public String getTag() {
            String tag = super.getTag();
            if (tag != null) {
                return tag;
            }
            Object[] stackTrace = new Throwable().getStackTrace();
            for (Object element : stackTrace) {
                StackTraceElement it = (StackTraceElement) element;
                if (!this.fqcnIgnore.contains(it.getClassName())) {
                    return createStackElementTag(it);
                }
            }
            throw new NoSuchElementException("Array contains no element matching the predicate.");
        }


        protected String createStackElementTag(StackTraceElement element) {
            String className = element.getClassName();
            int index = className.lastIndexOf(".");
            String tag = className;
            if (index != -1) {
                tag = className.substring(index + 1);
            }
            Matcher m = ANONYMOUS_CLASS.matcher(tag);
            if (m.find()) {
                tag = m.replaceAll("");
            }
            tag += "." + element.getMethodName();
            if (tag.length() <= MAX_TAG_LENGTH) {
                return tag;
            }
            String substring = tag.substring(0, MAX_TAG_LENGTH);
            return substring;
        }

        @Override
        public void log(int priority, String tag, String message, Throwable t) {
            if (message.length() < MAX_LOG_LENGTH) {
                if (priority == 7) {
                    wtf(tag, message);
                    return;
                } else {
                    println(priority, tag, message);
                    return;
                }
            }
            int i = 0;
            int length = message.length();
            while (i < length) {
                int newline = message.indexOf('\n', i);
                int newline2 = newline != -1 ? newline : length;
                do {
                    int end = Math.min(newline2, i + MAX_LOG_LENGTH);
                    String part = message.substring(i, end);
                    if (priority == 7) {
                        wtf(tag, part);
                    } else {
                        println(priority, tag, part);
                    }
                    i = end;
                } while (i < newline2);
                i++;
            }
        }

        public static void wtf(String tag, String message) {
            println(7, tag, message);
        }

        public static void println(int priority, String tag, String msg) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(dateTimeFormatter.format(LocalDateTime.now())).append("\t");
            switch (priority) {
                case Log.VERBOSE:
                    buffer.append("\u001B[30m  V\t"); // Black
                    break;
                case Log.DEBUG:
                    buffer.append("\u001B[32m  D\t"); // Green
                    break;
                case Log.INFO:
                    buffer.append("\u001B[34m I\t"); // Blue
                    break;
                case Log.WARN:
                    buffer.append("\u001B[33m W\t"); // Yellow
                    break;
                case Log.ERROR:
                    buffer.append("\u001B[31m E\t"); // Red
                    break;
                case Log.ASSERT:
                    buffer.append("\u001B[35m A\t"); // Magenta
                    break;
                default:
                    buffer.append("\u001B[0m o\t");
            }
            buffer.append(tag).append("\t").append(msg);
            buffer.append("\u001B[0m ");
            System.out.println(buffer);
        }
    }

    public static final class Forest extends Tree {
        public Forest() {

        }

        @Override

        public void v(String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.v(message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void v(Throwable t, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.v(t, message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void v(Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.v(t);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void d(String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.d(message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void d(Throwable t, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.d(t, message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void d(Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.d(t);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void i(String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.i(message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void i(Throwable t, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.i(t, message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void i(Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.i(t);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void w(String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.w(message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void w(Throwable t, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.w(t, message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void w(Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.w(t);
            }
        }

        @Override
        public void e(String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.e(message, args);
            }
        }

        @Override
        public void e(Throwable t, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.e(t, message, args);
            }
        }

        @Override
        public void e(Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.e(t);
            }
        }

        @Override
        public void s(String message, Object... args) {
            for (Tree tree : Log.treeArray) {
                tree.s(message, args);
            }
        }

        @Override
        public void s(Throwable t, String message, Object... args) {
            for (Tree tree : Log.treeArray) {
                tree.s(t, message, args);
            }
        }

        @Override
        public void s(Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.s(t);
            }
        }

        @Override
        public void wtf(String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.wtf(message, args);
            }
        }

        @Override
        public void wtf(Throwable t, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.wtf(t, message, args);
            }
        }

        @Override // com.start.outercasing.log.Timber.Tree

        public void wtf(Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.wtf(t);
            }
        }

        @Override
        public void log(int priority, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.log(priority, message, args);
            }
        }

        @Override
        public void log(int priority, Throwable t, String message, Object... args) {

            for (Tree tree : Log.treeArray) {
                tree.log(priority, t, message, args);
            }
        }

        @Override
        public void log(int priority, Throwable t) {
            for (Tree tree : Log.treeArray) {
                tree.log(priority, t);
            }
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            throw new AssertionError();
        }


        public Tree asTree() {
            return this;
        }


        public final Tree tag(String tag) {
            Tree[] treeArr;
            for (Tree tree : Log.treeArray) {
                tree.getExplicitTag$outer_casing_debug().set(tag);
            }
            return this;
        }

        public final void plant(Tree tree) {
            if (tree != this) {
                synchronized (Log.trees) {
                    Log.trees.add(tree);
                    Forest forest = Log.Forest;
                    Collection $this$toTypedArray$iv = Log.trees;
                    Object[] array = $this$toTypedArray$iv.toArray(new Tree[0]);
                    Log.treeArray = (Tree[]) array;
                }
                return;
            }
            throw new IllegalArgumentException("Cannot plant Timber into itself.".toString());
        }


        public final void plant(Tree... trees) {
            int length = trees.length;
            for (int i = 0; i < length; i++) {
                Tree tree = trees[i];
                if (tree == null) {
                    throw new IllegalArgumentException("trees contained null".toString());
                }
                if (!(tree != this)) {
                    throw new IllegalArgumentException("Cannot plant Timber into itself.".toString());
                }
            }
            synchronized (Log.trees) {
                Collections.addAll(Log.trees, Arrays.copyOf(trees, trees.length));
                Forest forest = Log.Forest;
                Collection $this$toTypedArray$iv = Log.trees;
                Object[] array = $this$toTypedArray$iv.toArray(new Tree[0]);
                Log.treeArray = (Tree[]) array;
            }
        }


        public final void uproot(Tree tree) {
            synchronized (Log.trees) {
                if (!Log.trees.remove(tree)) {
                    throw new IllegalArgumentException(("Cannot uproot tree which is not planted: " + tree).toString());
                }
                Forest forest = Log.Forest;
                Collection $this$toTypedArray$iv = Log.trees;
                Object[] array = $this$toTypedArray$iv.toArray(new Tree[0]);
                Log.treeArray = (Tree[]) array;

            }
        }


        public final void uprootAll() {
            synchronized (Log.trees) {
                Log.trees.clear();
                Forest forest = Log.Forest;
                Log.treeArray = new Tree[0];
            }
        }


        public final List<Tree> forest() {
            List<Tree> unmodifiableList;
            synchronized (Log.trees) {
                unmodifiableList = Collections.unmodifiableList(Log.trees);
            }
            return unmodifiableList;
        }


        public final int treeCount() {
            return Log.treeArray.length;
        }
    }
}