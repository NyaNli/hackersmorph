package nyanli.hackersmorph.util;

import java.util.Arrays;

public class MatrixTools {
	
	public static void inverse(double[] mat) {
		// TODO: throw exception if blahblahblahblah
		
		double[] input = Arrays.copyOf(mat, mat.length);
		
		// main diagonal line cannot have 0
		int length = (int) Math.sqrt(input.length);
		
		/*
		 * 我惊了我学了一天的LU分解只用到5行代码
		 * LU Decomposition without P
		 * | U U U U U U |
		 * | L U U U U U |
		 * | L L U U U U |
		 * | L L L U U U |
		 * | L L L L U U |
		 * | L L L L L U |
		 */
		for (int i = 0; i < length - 1; i++) {
			for (int j = i + 1; j < length; j++) {
				input[j * length + i] /= input[i * length + i];
				for (int k = i + 1; k < length; k++)
					input[j * length + k] -= input[j * length + i] * input[i * length + k];
				
				// calc L Back Substitution at the same time
				for (int k = 0; k <= i; k++) {
					if (k < i)
						input[j * length + k] += input[i * length + k] * input[j * length + i];
					if (j == i + 1)
						input[j * length + k] *= -1;
				}
			}
		}
		
		// U Back Substitution
		for (int i = 0; i < length; i++) {
			for (int j = length - 1; j >= 0; j--) {
				double result = j <= i ? j < i ? 0 : 1 : input[j * length + i];
				for (int k = length - 1; k > j; k--)
					result -= mat[k * length + i] * input[j * length + k];
				mat[j * length + i] = result / input[j * length + j];
			}
		}
	}
	
	public static void leftMul(double[] mat, double[] vec) {
		// TODO: throw exception if blahblahblahblah
		
		double[] input = Arrays.copyOf(vec, vec.length);
		
		for (int i = 0; i < vec.length; i++) {
			vec[i] = 0;
			for (int j = 0; j < vec.length; j++) {
				vec[i] += mat[i * vec.length + j] * input[j];
			}
		}
	}
	
	public static void solve(double[] mat, double[] vec, int length) {
		// TODO: throw exception if blahblahblahblah
		
		/*
		 * LU Decomposition without P
		 * | U U U U U U |
		 * | L U U U U U |
		 * | L L U U U U |
		 * | L L L U U U |
		 * | L L L L U U |
		 * | L L L L L U |
		 */
		for (int i = 0; i < length - 1; i++) {
			for (int j = i + 1; j < length; j++) {
				mat[j * length + i] /= mat[i * length + i];
				for (int k = i + 1; k < length; k++)
					mat[j * length + k] -= mat[j * length + i] * mat[i * length + k];
			}
		}
		
		// L Back Substitution
		for (int i = 1; i < length; i++) {
			for (int j = 0; j < i; j++)
				vec[i] -= vec[j] * mat[i * length + j];
		}
		
		// U Back Substitution
		for (int i = length - 1; i >= 0; i--) {
			for (int j = length - 1; j > i; j--)
				vec[i] -= vec[j] * mat[i * length + j];
			vec[i] /= mat[i * length + i];
		}
	}
	
	public static void fillData(double[] mat, int length, int row, int col, double...data)  {
		for (int i = 0; i < data.length && col + i < length; i++)
			mat[row * length + col + i] = data[i];
	}

}
