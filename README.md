Quick-n-dirty repo to preprocess CORD-19 data to format it for rhapsode.

Full citation for the data:
COVID-19 Open Research Dataset (CORD-19). 2020. 
Version 2020-03-20. 
Retrieved from https://pages.semanticscholar.org/coronavirus-research. 
Accessed 2020-03-23. doi:10.5281/zenodo.3715506

Data links:
 * https://ai2-semanticscholar-cord-19.s3-us-west-2.amazonaws.com/2020-03-20/comm_use_subset.tar.gz
 * https://ai2-semanticscholar-cord-19.s3-us-west-2.amazonaws.com/2020-03-20/noncomm_use_subset.tar.gz
 * https://ai2-semanticscholar-cord-19.s3-us-west-2.amazonaws.com/2020-03-20/custom_license.tar.gz
 * https://ai2-semanticscholar-cord-19.s3-us-west-2.amazonaws.com/2020-03-20/biorxiv_medrxiv.tar.gz
 * https://ai2-semanticscholar-cord-19.s3-us-west-2.amazonaws.com/2020-03-20/metadata.csv

See search application here: https://rhapsode.covid19data.space/rhapsode/

As is immediately obvious, the application was not built by an UI designer, 
but rather only to prove the capabilities of concordance and cooccurrence search
along with some other capabilities.  See also LUCENE-5205...

The source code for the search application is here: https://github.com/tballison/rhapsode

Check out these other search engines for the CORD-19 data:
 * https://app.sketchengine.eu/#dashboard?corpname=preloaded%2Fcovid19 (https://twitter.com/SketchEngine/status/1243228736490803205?s=20)
 * https://www.covidsearch.io/ (https://twitter.com/lintool/status/1241881933031841800?s=20)
 * http://covidseer.ist.psu.edu/
 * https://cord19.vespa.ai/

Relevant links:
 * https://twitter.com/mikemccand/status/1241682550969397249?s=20
 * https://www.infoworld.com/article/3533269/kaggle-calls-data-scientists-to-action-on-covid-19.html
 * https://registry.opendata.aws/cord-19/
 * https://www.linkedin.com/pulse/join-fight-against-coronavirus-covid-19-mike-honey