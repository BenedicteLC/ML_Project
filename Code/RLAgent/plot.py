import numpy as np
import matplotlib.pyplot as plt

results = np.loadtxt(open("Results.csv","rb"),delimiter=",",skiprows=1)
print results.shape

# Only plot one graph at a time!!!

# plt.plot(results[:,0], results[:,1], 'ro')
# plt.errorbar(results[:,0], results[:,1], yerr=results[:,4])
# plt.ylabel('Monster kills count')

plt.plot(results[:,0], results[:,2], 'bo')
plt.errorbar(results[:,0], results[:,2], yerr=results[:,5])
plt.ylabel('Winning probability')

# plt.plot(results[:,0], results[:,3], 'go')
# plt.errorbar(results[:,0], results[:,3], yerr=results[:,6])
# plt.ylabel('Fractions of cells passed')

plt.xlabel('Train interation')
plt.show()