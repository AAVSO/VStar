# Diff between two candidate HJD values and analysis
# see https://github.com/AAVSO/VStar/issues/88
 
import numpy as np

def hjd_diff(hjd1, hjd2, verbose=True):
  abs_delta = abs(hjd1-hjd2)
  delta_secs = abs_delta*24*60*60
  if verbose:
    print(hjd1, hjd2, abs_delta, delta_secs)
  return abs_delta, delta_secs


def analysis(hjd_pairs, title):
  print(title)

  results = [hjd_diff(*pair) for pair in hjd_pairs]

  deltas = np.array([result[0] for result in results])
  print(deltas.mean(), deltas.std())

  
def test():
  old = [[2448908.497815484, 2448908.4978207937], [2457501.8694125116, 2457501.869426729], [2448908.497780874, 2448908.4977766555], [2457501.870747149, 2457501.8707473096], [2448908.499268005, 2448908.4992657346], [2457501.868574388, 2457501.8685732614]]

  new_eccentricity_fix = [[2448908.4978205077, 2448908.4978207937], [2457501.869423921, 2457501.869426729], [2448908.4977859845, 2448908.4977766555], [2457501.870765871, 2457501.8707473096], [2448908.499269691, 2448908.4992657346], [2457501.8685812056, 2457501.8685732614]]

  new_long2000_fix = [[2448908.497820837, 2448908.4978207937], [2457501.869426577, 2457501.869426729], [2448908.4977756487, 2448908.4977766555], [2457501.8707476617, 2457501.8707473096], [2448908.4992653183, 2448908.4992657346], [2457501.8685733136, 2457501.8685732614]]

  analysis(old, "old")
  analysis(new_eccentricity_fix, "new eccentricity fix")
  analysis(new_long2000_fix, "new longitude 2000 fix")
    

if __name__ == "__main__":
  test()
