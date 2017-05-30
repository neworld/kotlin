/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.allopen.ide

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.allopen.AbstractAllOpenDeclarationAttributeAltererExtension
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.allopen.AllOpenCommandLineProcessor.Companion.PLUGIN_ID
import org.jetbrains.kotlin.allopen.AllOpenCommandLineProcessor.Companion.ANNOTATION_OPTION
import org.jetbrains.kotlin.idea.util.MODALITY_IS_ALTERED
import org.jetbrains.kotlin.psi.KtElement
import java.util.*

class IdeAllOpenDeclarationAttributeAltererExtension(val project: Project) : AbstractAllOpenDeclarationAttributeAltererExtension() {
    private companion object {
        val ANNOTATION_OPTION_PREFIX = "plugin:$PLUGIN_ID:${ANNOTATION_OPTION.name}="
    }

    private val cache: CachedValue<WeakHashMap<Module, List<String>>> = cachedValue(project) {
        CachedValueProvider.Result.create(WeakHashMap<Module, List<String>>(), ProjectRootModificationTracker.getInstance(project))
    }

    override fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String> {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return ANNOTATIONS_FOR_TESTS
        }

        if (modifierListOwner == null) return emptyList()
        val module = ModuleUtilCore.findModuleForPsiElement(modifierListOwner) ?: return emptyList()

        return cache.value.getOrPut(module) {
            val kotlinFacet = KotlinFacet.get(module) ?: return@getOrPut emptyList()
            val commonArgs = kotlinFacet.configuration.settings.compilerArguments ?: return@getOrPut emptyList()

            commonArgs.pluginOptions?.filter { it.startsWith(ANNOTATION_OPTION_PREFIX) }
                                    ?.map { it.substring(ANNOTATION_OPTION_PREFIX.length) }
                                    ?: emptyList()
        }
    }

    override fun recordModalityWasAltered(element: KtElement, isAltered: Boolean) {
        if (isAltered) {
            element.putUserData(MODALITY_IS_ALTERED, true)
        } else {
            element.removeUserData(MODALITY_IS_ALTERED)
        }
    }

    private fun <T : Any> UserDataHolder.removeUserData(key: Key<T>) {
        putUserData(key, null)
    }

    private fun <T> cachedValue(project: Project, result: () -> CachedValueProvider.Result<T>): CachedValue<T> {
        return CachedValuesManager.getManager(project).createCachedValue(result, false)
    }
}