package io.github.mrvanish97.kbot.controller

import io.github.mrvanish97.kbot.controller.node.ExecutionPredicate

interface StateService<View : Any, Updater : Any, PredicateValue : Any> :
  ExecutionPredicate<PredicateValue> {

  val updater: Updater

  val current: View

}