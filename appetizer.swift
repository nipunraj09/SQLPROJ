//
//  Appetizerlistview.swift
//  FoodApp
//
//  Created by pc on 14/06/25.
//

import SwiftUI

struct Appetizerlistview: View {
    var body: some View {
        NavigationView{
            List(MockData.Foods,id:\.id){food in
                HStack{
                    Image("asian-flank-steak")
                        .resizable()
                        .aspectRatio(contentMode :.fit)
                        .frame(width:120,height:50,alignment:.center)
                    
                    VStack(alignment:.leading, spacing: 5){
                        Text(food.name)
                            .font(.title2)
                            .fontWeight(.medium)
                        
                        Text("$\(food.price, specifier:"%.2f")")
                            .foregroundColor(.secondary)
                            .fontWeight(.semibold)
                            
                    }
                }
            }
            .navigationTitle("Foods 🍕")
            
        }
    }
}

#Preview {
    Appetizerlistview()
}
