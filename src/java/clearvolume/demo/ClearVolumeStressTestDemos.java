package clearvolume.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Test;

import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.clearcuda.JCudaClearVolumeRenderer;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunctions;

import com.jogamp.newt.awt.NewtCanvasAWT;

import coremem.types.NativeTypeEnum;

public class ClearVolumeStressTestDemos
{

	public static void main(final String[] argv) throws ClassNotFoundException
	{
		if (argv.length == 0)
		{
			final Class<?> c = Class.forName("clearvolume.demo.ClearVolumeStressTestDemos");

			System.out.println("Give one of the following method names as parameter:");

			for (final Member m : c.getMethods())
			{
				final String name = ((Method) m).getName();

				if (name.substring(0, 4).equals("demo"))
				{
					System.out.println("Demo: " + ((Method) m).getName());
				}
			}
		}
		else
		{
			final ClearVolumeStressTestDemos cvdemo = new ClearVolumeStressTestDemos();
			Method m;

			try
			{
				m = cvdemo.getClass().getMethod(argv[0]);
			}
			catch (final Exception e)
			{
				System.out.println("Could not launch " + argv[0]
														+ " because ...");
				e.printStackTrace();

				return;
			}

			try
			{
				System.out.println("Running " + argv[0] + "()...");
				m.invoke(cvdemo);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}

	}


	@Test
	public void demoStressTest() throws InterruptedException,
															IOException
	{
		for (int i = 0; i < 50; i++)
		{

			final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
																																																						i % 2 == 0 ? 768
																																																											: 512,
																																																						i % 2 == 0 ? 768
																																																											: 512,
																																																						NativeTypeEnum.UnsignedByte,
																																																						false);
			lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
			lClearVolumeRenderer.setVisible(true);

			final int lResolutionX = 512;
			final int lResolutionY = lResolutionX;
			final int lResolutionZ = lResolutionX;

			final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
																								* lResolutionZ
																								* 2];

			for (int z = 0; z < lResolutionZ; z++)
				for (int y = 0; y < lResolutionY; y++)
					for (int x = 0; x < lResolutionX; x++)
					{
						final int lIndex = 2 * (x + lResolutionX * y + lResolutionX * lResolutionY
																														* z);
						lVolumeDataArray[lIndex + 1] = (byte) (((byte) x ^ (byte) y ^ (byte) z));
					}

			lClearVolumeRenderer.setVolumeDataBuffer(	0,
																								ByteBuffer.wrap(lVolumeDataArray),
																								lResolutionX,
																								lResolutionY,
																								lResolutionZ);
			lClearVolumeRenderer.requestDisplay();

			// Thread.sleep(100);

			lClearVolumeRenderer.close();
		}

	}

	@Test
	public void demoStressTestNewtCanvasAWT()	throws InterruptedException,
																						IOException,
																						InvocationTargetException
	{
		for (int nfi = 0; nfi < 50; nfi++)
		{
			final int i = nfi;

			final JFrame lJFrame = new JFrame("ClearVolume");
			;

			final ClearVolumeRendererInterface lClearVolumeRenderer = new JCudaClearVolumeRenderer(	"ClearVolumeTest",
																																															(i % 2 == 0) ? 768
																																																					: 64,
																																															(i % 2 == 0) ? 768
																																																					: 64,
																																															NativeTypeEnum.UnsignedByte,
																																															(i % 2 == 0) ? 768
																																																					: 64,
																																															(i % 2 == 0) ? 768
																																																					: 64,
																																															1,
																																															true);

			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{

					/*
					 * final ClearVolumeRendererInterface lClearVolumeRenderer =
					 * ClearVolumeRendererFactory.newBestRenderer(
					 * "ClearVolumeTest", i % 2 == 0 ? 768 : 512, i % 2 == 0 ?
					 * 768 : 512, 1, i % 2 == 0 ? 768 : 512, i % 2 == 0 ? 768 :
					 * 512, 1, true);/*
					 */
					final NewtCanvasAWT lNewtCanvasAWT = lClearVolumeRenderer.getNewtCanvasAWT();

					System.out.println("lJFrame.setTitle(...");
					lJFrame.setTitle("BRAVO! THIS IS A JFRAME! It WORKS! I=" + i);
					lJFrame.setLayout(new BorderLayout());
					final Container lContainer = new Container();
					lContainer.setLayout(new BorderLayout());

					System.out.println("lContainer.add(lNewtCanvasAWT, BorderLayout.CENTER);");
					lContainer.add(lNewtCanvasAWT, BorderLayout.CENTER);
					lJFrame.setSize(new Dimension(1024, 1024));
					lJFrame.add(lContainer);

					System.out.println("lJFrame.setVisible(true);");
					lJFrame.setVisible(true);
				}
			});

			lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
			lClearVolumeRenderer.setVisible(true);

			final int lResolutionX = 512;
			final int lResolutionY = lResolutionX;
			final int lResolutionZ = lResolutionX;

			final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
																								* lResolutionZ];

			for (int d = 0; d < 1; d++)
			{
				for (int z = 0; z < lResolutionZ; z++)
					for (int y = 0; y < lResolutionY; y++)
						for (int x = 0; x < lResolutionX; x++)
						{
							final int lIndex = x + lResolutionX
																	* y
																	+ lResolutionX
																	* lResolutionY
																	* z;
							int lCharValue = (((byte) x ^ (byte) y ^ (byte) z ^ (byte) d));
							if (lCharValue < 12)
								lCharValue = 0;
							lVolumeDataArray[lIndex] = (byte) lCharValue;
						}

				lClearVolumeRenderer.setCurrentRenderLayer(0);
				lClearVolumeRenderer.setVolumeDataBuffer(	0,
																									ByteBuffer.wrap(lVolumeDataArray),
																									lResolutionX,
																									lResolutionY,
																									lResolutionZ);

				System.out.println("lClearVolumeRenderer.requestDisplay();");
				lClearVolumeRenderer.requestDisplay();
			}

			/*
			 * try { //Thread.sleep(500); } catch (final InterruptedException e)
			 * { e.printStackTrace(); }/*
			 */

			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					System.out.println("lJFrame.setVisible(true); lJFrame.dispose();");
					// lJFrame.setVisible(false);
					lJFrame.dispose();
				}
			});/**/

			lClearVolumeRenderer.close();
		}

	}


}