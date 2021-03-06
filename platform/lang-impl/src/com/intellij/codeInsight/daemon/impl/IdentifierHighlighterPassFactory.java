// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.daemon.impl;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

public class IdentifierHighlighterPassFactory implements TextEditorHighlightingPassFactory {
  private static final int[] AFTER_PASSES = {Pass.UPDATE_ALL};

  private static boolean ourTestingIdentifierHighlighting;
  private final Project myProject;

  public IdentifierHighlighterPassFactory(Project project, TextEditorHighlightingPassRegistrar highlightingPassRegistrar) {
    myProject = project;
    highlightingPassRegistrar.registerTextEditorHighlightingPass(this, null, AFTER_PASSES, false, -1);
  }

  @Override
  public TextEditorHighlightingPass createHighlightingPass(@NotNull final PsiFile file, @NotNull final Editor editor) {
    if (!editor.isOneLineMode() &&
        CodeInsightSettings.getInstance().HIGHLIGHT_IDENTIFIER_UNDER_CARET &&
        !DumbService.isDumb(myProject) &&
        (!ApplicationManager.getApplication().isUnitTestMode() || ourTestingIdentifierHighlighting) &&
        (file.isPhysical() || file.getOriginalFile().isPhysical())) {
      return new IdentifierHighlighterPass(file.getProject(), file, editor);
    }

    return null;
  }

  @TestOnly
  public static void doWithHighlightingEnabled(@NotNull Runnable r) {
    ourTestingIdentifierHighlighting = true;
    try {
      r.run();
    }
    finally {
      ourTestingIdentifierHighlighting = false;
    }
  }
}
