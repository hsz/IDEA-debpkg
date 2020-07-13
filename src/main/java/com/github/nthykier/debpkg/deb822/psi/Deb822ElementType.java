package com.github.nthykier.debpkg.deb822.psi;

import com.github.nthykier.debpkg.deb822.Deb822Language;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class Deb822ElementType extends IElementType {
    public Deb822ElementType(@NotNull @NonNls String debugName) {
        super(debugName, Deb822Language.INSTANCE);
    }

    @Override
    public String toString() {
        return "Deb822ElementType." + super.toString();
    }
}