import 'package:flutter/material.dart';

Rect buildRectFor(BuildContext context) {
  final mediaQuery = MediaQuery.of(context);
  var top = mediaQuery.padding.top;
  var height = mediaQuery.size.height - top;

  if (height < 0.0) {
    height = 0.0;
  }

  return Rect.fromLTWH(0.0, top, mediaQuery.size.width, height);
}
