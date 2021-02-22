# Hacker's Morph

___两个坏消息___  
___1. 原作者不更新了___  
___2. 第一个坏消息是真的___

----------------------------

<s>本Mod中包含了对Optifine的修改和引用，不过Optifine的FAQ对Can I include OptiFine in my modpack?的回答是Generally no, unless you have an explicit permission from us.说实话不太明白include Optifine in my modpack是什么意思，是指mod以Optifine作为了前置mod？还是在mod发布时“包含”了Optifine？说实话我不太清楚= =总之如果这方面出了问题就删库跑路呗，虽然已经在跑路了</s>  
经过大佬解答引用Optifine作为前置Mod似乎是没问题的\[issue#1\]，但我不管我依然要跑路

----------------------------

这是一个对McHorse所开发的短片制作系列模组进行修改及功能追加的外挂型模组  
本模组修改&增加了以下功能  

## McLib（2.1-2.2.2）
+ 修复了Trackpad Element调用Callback时传入超出范围数值的bug（该提个issue的）
+ 修了一下2.2的1283报错（虽然马哥在2.2.1也修了但没有push/popMatrix我不是很安心所以自己加了一层）
+ 位移缩放旋转控件组增加一组相对父坐标轴旋转的控件（虽然转起来更符合常识了但大部分情况下计算出的旋转值会反 复 横 跳导致关键帧动画炸裂，所以不建议在需要做旋转动画的地方使用）

## Metamorph（1.2.3-1.2.5）
+ 肢体部件编辑器现在按住Ctrl键切换肢体可以自动转换坐标，以保持肢体部件在空间中的位置（仅在肢体的xyz轴缩放值相同的情况可用，因为缩放值不同会导致转换矩阵变成非正交无法计算转换后的坐标，虽然可以用空伪装包装一层再做计算实现转换但是我怎么知道我这么个小肢体部件套在了个什么玩意上面，代码层面不通用啊= =）

## Blockbuster（2.1-2.2.2）
+ 新增空回放生成命令/record blank（如果服务器上有别人在编辑同名回放……大概会炸（<i>服务器：我怎么知道谁在编辑这玩意</i>））
+ 播放回放会显示“正在播放XXX”的提示（仅播放者能看到）
+ 鼠标指向受破坏控制的方块现在会出现提示（不仔细看根本注意不到的那种）
+ 雪暴粒子现在会存储在伪装中以方便联机传输（但自己做的贴图不会传输）
+ 结构伪装重写了渲染方法并增加了以下功能
  + 手动重载（多个存档如果有同名的结构用这个就很方便切换，当然也可以用2.2之后的/model clear_structures命令全部重载）
  + 优先渲染（在与外部方块重合时控制渲染优先级，仅完整方块有用）
  + 伪造法线（将模型法线固定为指向天空，影响光影的最大光照计算，可在NBT中根据需要修改法线，仅非实体方块有用）
  + 独立光照（忽略世界光照并启用结构中方块产生的光照）
  + 自定义光照（开启独立光照后可以使用曲线调整光照的亮度）
  + 自定义生物群系（似乎只影响草方块的颜色）
  + 现在可以加载结构中的实体了（比如物品展示框、画、盔甲架什么的，当然都是不会动的，另外生物的身体旋转数据会消失，似乎是因为这个数值只存储在客户端？）
  + 现在结构里放模型方块渲染自己的结构伪装这样玩套娃不会死档了，套娃状态的结构伪装只会渲染10次，次数无法修改（话说谁没事套娃玩啊（<i>我啊我啊</i>））
+ 模型方块在关闭阴影选项后也不会有光影的阴影（但使用屏幕空间阴影修正生成的阴影不会消失）
+ 回放的名称标签以#ghost结尾时演员不会有光影的阴影（同上）
+ 序列发生器增加循环位移与洋葱皮功能
+ 序列发生器预览优化（2.2的编辑界面与Aperture相机编辑界面）
+ 针对序列发生器使用Emoticons模型时贴图切换会有1tick延迟的问题做了个小修复
+ 针对Aperture相机编辑界面的回放优化
+ 回放编辑器的伪装动作增加预览场景与洋葱皮功能（仅开启Aperture相机编辑界面有效）

## Aperture（1.5-1.5.2）
+ 修了个相机配置文件中只有第一个被加载的配置会加载曲线其他曲线都不会被加载的Bug（我确实该给这个提个issue= =）
+ 更多曲线参数&光影兼容（手动对焦不用再改光影啦）
+ __\[实验性\]__ 曲线参数控制光影配置文件（需手动在Mod设置里开启，会导致帧数稍微下降一点，20%左右吧）
+ 增加了配合Metamorph伪装实现的相机追踪功能（可以做自拍杆效果，相机伪装的关键帧是相对与相机伪装的而不是相对于世界空间的）
+ 关闭相机编辑界面后自动停止播放场景（可能只是我觉着这样好用？）

## 其他优化
+ 统一文件保存编码为UTF-8，旧文件中如果有中文可能会炸裂
+ __\[实验性\]__ /relight指令用于重算周围光照，写着玩的……

### 其他想写没写的：
+ 伪装动作预览统一，Aperture界面预览的时候，对于原版的人物移动、命令、方块破坏这些显示的其实是当前Tick结束时的情况，而Aperture镜头、Blockbuster伪装显示的则是当前Tick开始的情况，这样具体做的时候就会有0.05秒的很微妙的差错，因为伪装能用的动作太多了我分不清哪些是下一Tick客户端才能显示的，所以只做了人物运动部分预览的统一
+ 光影的阴影光控制，写起来也不是很麻烦，但想不出来怎么整合到这些Mod里，而且大多数光影包都是以平行光的方式对光照进行计算的，这样如果要做聚光灯的话，基于法线计算的最大光照就会有很微妙的差错，总之不是我能力的问题都是世界的错嗯
+ emoticon导入模型功能，blender导出bobj的脚本我都写好了，想了想还是算了= =马哥不开源这个总归是有原因的，虽然Optifine也没开源就让我给拆了（就直说你搞不定反混淆呗（闭嘴））

比起是要发布的Mod，更像是留给我自己做动画用的，因为就连我自己用上这个Mod做动画的过程中都有各种各样新鲜的Bug蹦出来……但我自己能自己修，如果发出来别人用出现Bug就没法修了= =  
另外我倒是很热衷于把这个Mod的commit清空掉……因为实在不想在搞更新了啊……  
要是修改一下那个垃圾ASM框架说不定jar包能缩小100KB，写之前实在没想到class增多是jar包变大的主要原因……
