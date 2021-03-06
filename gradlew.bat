import 'package:ast_biglietteria/components/ResumePurchase/resume_purchase.dart';
import 'package:ast_biglietteria/redux/store/app_state.dart';
import 'package:ast_biglietteria/screens/ResumeTicketPurchase/components/resume_item.dart';
import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';

class ResumePassPurchase extends StatefulWidget {
  @override
  _ResumePassPurchaseState createState() => _ResumePassPurchaseState();
}

class _ResumePassPurchaseState extends State<ResumePassPurchase> {
  PageController pageController = PageController();
  @override
  Widget build(BuildContext context) {
    return StoreConnector<AppState, Map>(
      converter: (store) => store.state.buyUrbano,
      builder: (context, buyUrbano) {
        var urbanPass = buyUrbano['buyUrbanoPass'];
        return ResumePurchase(
          height: 50,
          childCardBody: Padding(
            padding: EdgeInsets.all(8.0),
            child: ListView(
              children: <Widget>[
                ResumeItem(label: "Nome", content: "Pippo Pippino",),
                ResumeItem(label: "Codice Fiscale", content: "GGGGGGGGGGGGGGGGGGGGGG",),
                ResumeItem(label: "Tipo di abbonamento", content: buyUrbano['city'],),
                ResumeItem(label: "Tipo di abbonamento", content: urbanPass['pass']['name'],),
                ResumeItem(label: "Data di inizio", content: urbanPass['pass']['initData'],),
                ResumeItem(label: "Data di fine", content: urbanPass['pass']['finalData'],),
                ResumeItem(label: "Importo", content: urbanPass['pass']['price'],),
              ],
            ),
          ),
        );
      },
    );
  }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        