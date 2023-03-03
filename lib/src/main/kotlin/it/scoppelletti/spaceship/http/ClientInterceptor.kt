/*
 * Copyright (C) 2017 Dario Scoppelletti, <http://www.scoppelletti.it/>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.scoppelletti.spaceship.http

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import it.scoppelletti.spaceship.ApplicationException
import it.scoppelletti.spaceship.app.AppExt
import it.scoppelletti.spaceship.i18n.AppMessages
import it.scoppelletti.spaceship.i18n.I18NProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private const val OS_NAME = "android"
private const val VALUE_SEP = ';'

/**
 * Decorates an HTTP request with infos describing the client.
 *
 * @since 1.0.0
 */
public class ClientInterceptor @Inject constructor(
        private val context: Context,
        private val packageMgr: PackageManager,
        private val i18nProvider: I18NProvider,
        private val appMessages: AppMessages
) : Interceptor {

    private val osName = buildString {
        append(OS_NAME)
        append(VALUE_SEP)
        append(Build.VERSION.SDK_INT)
    }

    private val applName: String

    init {
        applName = initApplName()
    }

    /**
     * Init the application name and version.
     *
     * @return The value.
     */
    private fun initApplName(): String {
        val packageInfo: PackageInfo

        val name = context.packageName
        try {
            packageInfo = AppExt.getPackageInfo(packageMgr, name, 0)
        } catch (ex: PackageManager.NameNotFoundException) {
            throw ApplicationException(appMessages.errorPackageNotFound(name),
                    ex)
        }

        return buildString {
            append(name)
            append(VALUE_SEP)
            append(AppExt.getVersion(packageInfo))
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response =
            chain.proceed(chain.request().newBuilder()
                    .header(HttpHeader.OS, osName)
                    .header(HttpHeader.APPL, applName)
                    .header(HttpHeader.LOCALE,
                            i18nProvider.currentLocale().toLanguageTag())
                    .header(HttpHeader.TIMEZONE,
                            i18nProvider.currentZoneId().id)
                    .build())
}

