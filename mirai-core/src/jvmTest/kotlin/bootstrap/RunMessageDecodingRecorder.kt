/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.bootstrap

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderProcessor
import net.mamoe.mirai.internal.testFramework.desensitizer.Desensitizer
import net.mamoe.mirai.internal.testFramework.message.protocol.MessageDecodingRecorder
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.readResource
import net.mamoe.yamlkt.Yaml
import kotlin.concurrent.thread

suspend fun main() {
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        Bot.instances.forEach {
            it.close()
        }
    })


    Desensitizer.local.desensitize("") // verify rules

    val account = Yaml.decodeFromString(LocalAccount.serializer(), readResource("local.account.yml"))
    val bot = BotFactory.newBot(account.id, account.password) {
        enableContactCache()
        fileBasedDeviceInfo("local.device.json")
        protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
    }.asQQAndroidBot()

    MessageProtocolFacade.decoderPipeline.registerBefore(MessageDecoderProcessor(MessageDecodingRecorder()))

    bot.login()

    bot.join()
}