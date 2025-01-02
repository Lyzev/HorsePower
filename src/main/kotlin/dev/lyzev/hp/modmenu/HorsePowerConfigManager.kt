/*
 * Copyright (c) 2025. Lyzev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.lyzev.hp.modmenu

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.terraformersmc.modmenu.ModMenu
import com.terraformersmc.modmenu.config.option.BooleanConfigOption
import com.terraformersmc.modmenu.config.option.ConfigOptionStorage
import com.terraformersmc.modmenu.config.option.EnumConfigOption
import com.terraformersmc.modmenu.config.option.StringSetConfigOption
import dev.lyzev.hp.HorsePower
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

object HorsePowerConfigManager {

    private val path: Path by lazy {
        FabricLoader.getInstance().configDir.resolve("${HorsePower.MOD_ID}.json")
    }

    fun initializeConfig() {
        load()
    }

    private fun load() {
        if (!Files.exists(path)) {
            save()
        }

        if (Files.exists(path)) {
            try {
                Files.newBufferedReader(path).use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject
                    HorsePowerConfig::class.declaredMemberProperties.forEach { property ->
                        property.isAccessible = true
                        when (val value = property.get(HorsePowerConfig)) {
                            is StringSetConfigOption -> json.getAsJsonArray(property.name.lowercase())?.let { jsonArray ->
                                ConfigOptionStorage.setStringSet(value.key, jsonArray.map { it.asString }.toSet())
                            }
                            is BooleanConfigOption -> json.getAsJsonPrimitive(property.name.lowercase())?.let { jsonPrimitive ->
                                if (jsonPrimitive.isBoolean) {
                                    ConfigOptionStorage.setBoolean(value.key, jsonPrimitive.asBoolean)
                                }
                            }
                            is EnumConfigOption<*> -> json.getAsJsonPrimitive(property.name.lowercase())?.let { jsonPrimitive ->
                                if (jsonPrimitive.isString) {
                                    val enumClass = (property.returnType.classifier as Class<Enum<*>>)
                                    val enumValue = enumClass.enumConstants.firstOrNull { it.name.equals(jsonPrimitive.asString, ignoreCase = true) }
                                    enumValue?.let { ConfigOptionStorage.setEnumTypeless(value.key, it) }
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                System.err.println("Couldn't load Horse Power configuration file; reverting to defaults")
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                System.err.println("Couldn't load Horse Power configuration file; reverting to defaults")
                e.printStackTrace()
            }
        }
    }

    fun save() {
        val config = JsonObject()

        HorsePowerConfig::class.declaredMemberProperties.forEach { property ->
            property.isAccessible = true
            when (val value = property.get(HorsePowerConfig)) {
                is BooleanConfigOption -> config.addProperty(property.name.lowercase(), ConfigOptionStorage.getBoolean(value.key))
                is StringSetConfigOption -> {
                    val jsonArray = JsonArray()
                    ConfigOptionStorage.getStringSet(value.key).forEach { jsonArray.add(it) }
                    config.add(property.name.lowercase(), jsonArray)
                }
                is EnumConfigOption<*> -> {
                    val enumValue = ConfigOptionStorage.getEnumTypeless(value.key, property.returnType.classifier as Class<Enum<*>>)
                    config.addProperty(property.name.lowercase(), enumValue.name.lowercase())
                }
            }
        }

        try {
            Files.newBufferedWriter(path).use { writer ->
                writer.write(ModMenu.GSON.toJson(config))
            }
        } catch (e: IOException) {
            System.err.println("Couldn't save Horse Power configuration file")
            e.printStackTrace()
        }
    }
}
