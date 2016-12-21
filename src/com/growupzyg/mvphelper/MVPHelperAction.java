package com.growupzyg.mvphelper;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiJavaFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 程序的入口
 * Created by zyg on 16/09/07.
 */
public class MVPHelperAction extends AnAction {
    private ClassModel _classModel;
    private Editor _editor;
    private String _content;
    private boolean canCreate;
    private AnActionEvent _event;
    private String _path;
    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here

        this._event = e;
        canCreate = true;
        init(e);
        getClassModel();
        createFiles();
        PsiJavaFile javaFile = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);

        System.out.println("current package name is :"+javaFile.getPackageName());
        try {
            if(canCreate) {
                createClassFiles();
                MessagesCenter.showMessage("created success! please wait a moment","success");
                refreshProject(e);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }

    private void refreshProject(AnActionEvent e) {
        e.getProject().getBaseDir().refresh(false,true);
        }

    /**
     * 创建class文件
     * create class files
     * @throws IOException
     */
    private void createClassFiles() throws IOException {
        createFileWithContract();
    }

    /**
     * 以contract模式生成 .java文件
     * @throws IOException
     */
    private void createFileWithContract() throws IOException {
        String className = _classModel.get_className();
        String classFullName = _classModel.get_classFullName();
        System.out.println("_path:" + _path);
        //创建Presenter
        ClassCreateHelper.createClasses(_path, className, classFullName, ClassCreateHelper.PRESENTER);
        //创建View
        ClassCreateHelper.createClasses(_path, className, classFullName, ClassCreateHelper.VIEW);
        /*//创建Activity
        ClassCreateHelper.createClasses(_path, className, classFullName, ClassCreateHelper.ACTIVITY);*/
    }


    /**
     * 生成 contract类内容
     * create Contract Model Presenter
     */
    private void createFiles() {
        if (null == _classModel.get_className()) {
            return;
        }
         _path= ClassCreateHelper.getCurrentPath(_event,_classModel.get_classFullName());
        System.out.println("current _path"+ _path);
        if(_classModel.get_classFullName().contains("Contract")) {

            System.out.println("_path replace contract "+ _path);
            _path = _path.replace("contract/", "");
        } else {
            MessagesCenter.showErrorMessage("Your FileName should end with 'Contract'.", "error");
            canCreate = false;
        }
        if(canCreate) {
            setFileDocument();
        }

    }

    /**
     * 生成 contract类内容
     * create Contract Model Presenter
     */
    private void setFileDocument() {
        String packageName = ClassCreateHelper.getPackageName(_path);

        String packageInfo = "package "+ packageName+";\n";
        String importInfo = "import cn.com.tcsl.spos.client.view.BaseFragmentView;\n";
        importInfo =  importInfo + "import cn.com.tcsl.spos.client.view.BasePresenter;\n";

        int packageIndex = _content.indexOf(packageInfo)+packageInfo.length();
        int lastIndex = _content.lastIndexOf("}");
        _content = packageInfo+importInfo+_content.substring(packageIndex, lastIndex);
        MessagesCenter.showDebugMessage(_content, "debug");
        final String content = setContractContent();
        //wirte in runWriteAction
        WriteCommandAction.runWriteCommandAction(_editor.getProject(),
                new Runnable() {
                    @Override
                    public void run() {
                        _editor.getDocument().setText(content);
                    }
                });

    }

    private String setContractContent() {
        String content = _content + "\tinterface View extends BaseFragmentView<Presenter> {\n\t}\n\n"
                + "\tinterface Presenter extends BasePresenter {\n\t}\n"
                + "\n}";

        return content;
    }


    private void getClassModel() {
        _content = _editor.getDocument().getText();

        String[] words = _content.split(" ");

        for (String word : words) {
            if (word.contains("Contract")) {
                String className = word.substring(0, word.indexOf("Contract"));
                _classModel.set_className(className);
                _classModel.set_classFullName(word);
                MessagesCenter.showDebugMessage(className, "class name");
            }
        }
        if (null == _classModel.get_className()) {
            MessagesCenter.showErrorMessage("Create failed ,Can't found 'Contract' in your class name,your class name must contain 'Contract'", "error");
            canCreate = false;
        }
    }

    private void init(AnActionEvent e) {
        _editor = e.getData(PlatformDataKeys.EDITOR);
        _classModel = new ClassModel();
    }


}
