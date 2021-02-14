package nyanli.hackersmorph.other.mchorse.blockbuster.common.block;

import mchorse.blockbuster.common.block.BlockGreen;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGreenExtra extends BlockGreen {
	
	// 降低亮度，使其不会传播光照到周围方块
	public BlockGreenExtra() {
		setLightLevel(1.0f / 15.0f);
	}

	// 修改渲染方块亮度为最亮
	@SideOnly(Side.CLIENT)
	@Override
	public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
		return 15728880; // 15 << 20 | 15 << 4
	}

	// 曾考虑通过修改这个使其在开启光影的情况下不渲染阴影
	// 但这个方法只有缓冲渲染区块的时候会执行，实际渲染并不会
	// 真要搞的话得用TileEntity
//	@Override
//	public EnumBlockRenderType getRenderType(IBlockState state) {
//		if (ShaderManager.isShadowPass())
//			return EnumBlockRenderType.INVISIBLE;
//		return EnumBlockRenderType.MODEL;
//	}

}
